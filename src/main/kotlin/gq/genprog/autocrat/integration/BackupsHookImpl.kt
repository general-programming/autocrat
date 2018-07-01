package gq.genprog.autocrat.integration

import gq.genprog.autocrat.config.AutocratConfig
import gq.genprog.autocrat.integration.sel.MutableSelection
import net.minecraft.init.Blocks
import net.minecraft.inventory.IInventory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.gen.structure.StructureBoundingBox
import silly511.backups.BackupManager
import silly511.backups.helpers.BackupHelper
import silly511.backups.util.CompressedRegionLoader
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class BackupsHookImpl: BackupsHook() {
    val dateFormat = DateTimeFormatter.ofPattern("yyyy/M/d-k:mm:ss")

    override fun isLoaded(): Boolean {
        return true
    }

    override fun restoreSelection(world: World, selection: MutableSelection, backupName: String): Boolean {
        val box = selection.toStructureBox() ?: return false
        val size = box.xSize * box.ySize * box.zSize
        val backup = this.parseBackupString(backupName) ?: return false

        if (!world.isAreaLoaded(box)) return false
        if (size > AutocratConfig.backups.maxRestoreSize) return false // TODO: Custom errors

        this.restoreBackup(world, backup, box)

        return true
    }

    override fun listBackups(world: World): List<String> {
        return this.getBackups().mapIndexed { i, backup ->
            val ago = Instant.now().until(backup.time, ChronoUnit.HOURS)

            "$i. ${backup.label} ($ago hours ago)"
        }
    }

    override fun completeBackupString(partial: String): MutableList<String> {
        val completions = mutableListOf<String>()

        val timeZone = ZoneId.systemDefault()
        completions.addAll(this.getBackups().map { it.time.atZone(timeZone).format(dateFormat) })
        completions.add("last")

        return completions
    }

    fun StructureBoundingBox.getAllBlocks(): Iterable<BlockPos> {
        return BlockPos.getAllInBox(minX, minY, minZ, maxX, maxY, maxZ)
    }

    fun restoreBackup(world: World, backup: BackupHelper.Backup, region: StructureBoundingBox): Int {
        val dimDir = world.provider.saveFolder
        val loadDir = if (dimDir == null) backup.dir else File(backup.dir, dimDir)
        val loader = CompressedRegionLoader(loadDir)

        val allBlocks = LinkedList<BlockPos>()
        var changes = 0

        for (pos in region.getAllBlocks()) {
            if (loader.getBlockState(pos) == null) continue

            val state = world.getBlockState(pos)
            if (!state.isFullBlock && !state.isFullCube)
                allBlocks.addFirst(pos)
            else
                allBlocks.addLast(pos)
        }

        for (pos in allBlocks) {
            val te = world.getTileEntity(pos)

            if (te is IInventory) te.clear()
            world.setBlockState(pos, Blocks.BARRIER.defaultState, 2)
        }

        for (pos in allBlocks) {
            if (world.setBlockState(pos, loader.getBlockState(pos), 2))
                changes++

            val te = world.getTileEntity(pos)
            val data = loader.getTileEntityData(pos)
            if (te != null && data != null) {
                te.readFromNBT(data)
                te.markDirty()
            }

            val tick = loader.getTileTick(pos)
            if (tick != null)
                world.scheduleBlockUpdate(pos, tick.block, tick.time, tick.priority)
        }

        return changes
    }

    fun parseBackupString(text: String): BackupHelper.Backup? {
        if (text.toLowerCase() == "last") {
            if (this.getLastBackupFile() == null)
                return null

            return BackupHelper.Backup.readBackup(this.getLastBackupFile())
        }

        val backups = this.getBackups()
        val numAgo = text.toIntOrNull()
        if (numAgo != null) {
            if (numAgo < 1) return null
            if (numAgo >= backups.size) return null

            return backups[numAgo - 1]
        }

        val time = LocalDateTime.parse(text, dateFormat).atZone(ZoneId.systemDefault()).toEpochSecond()
        return backups.stream().filter {
            it.time.epochSecond == time
        }.findFirst().orElse(null)
    }

    fun getBackups(): List<BackupHelper.Backup> {
        return BackupHelper.listAllBackups(BackupManager.getCurrentBackupsDir())
    }

    fun getLastBackupFile(): File? {
        return BackupHelper.getLastBackup(BackupManager.getCurrentBackupsDir())
    }
}
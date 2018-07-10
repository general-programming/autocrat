package gq.genprog.autocrat.transformer

import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ASM5
import org.objectweb.asm.Opcodes.GETSTATIC
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
class AutoClassTransformer: IClassTransformer {
    override fun transform(name: String, transformedName: String, basicClass: ByteArray?): ByteArray? {
        if (basicClass == null) return null
        if (name != "net.minecraft.network.NetHandlerPlayServer") return basicClass

        val node = ClassNode()
        val reader = ClassReader(basicClass)
        reader.accept(node, ClassReader.EXPAND_FRAMES)

        val i = node.methods.indexOfFirst { it.name == "update" }
        if (i < 0) return basicClass

        val method = node.methods[i]
        val newMethod = MethodNode(ASM5, method.access, method.name, method.desc, method.signature, method.exceptions.toTypedArray())

        val visitor = ModifyTimeoutAdapter(newMethod)
        method.accept(visitor)
        node.methods[i] = newMethod

        val writer = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        node.accept(writer)

        return writer.toByteArray()
    }

    class ModifyTimeoutAdapter(visitor: MethodVisitor?): MethodVisitor(ASM5, visitor) {
        override fun visitLdcInsn(cst: Any?) {
            if (cst == 15000L) {
                mv.visitFieldInsn(GETSTATIC, "gq/genprog/autocrat/helpers/Values", "keepAliveTimeout", "J")
            } else {
                super.visitLdcInsn(cst)
            }
        }
    }
}
package gq.genprog.autocrat

import gq.genprog.autocrat.config.AutocratConfig
import gq.genprog.autocrat.server.ServerProxy
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.ExtensionPoint
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.network.FMLNetworkConstants
import org.apache.commons.lang3.tuple.Pair
import thedarkcolour.kotlinforforge.forge.MOD_CONTEXT
import java.util.function.BiPredicate
import java.util.function.Supplier

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
@Mod(MOD_ID)
class Autocrat {
    init {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST) {
            Pair.of(
                    Supplier { FMLNetworkConstants.IGNORESERVERONLY },
                    BiPredicate { _, _ -> true }
            )
        }

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, AutocratConfig.spec)

        DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER) {
            DistExecutor.SafeRunnable {
                val proxy = ServerProxy()

                MOD_CONTEXT.getKEventBus().register(proxy)
                MinecraftForge.EVENT_BUS.register(proxy)
            }
        }
    }
}

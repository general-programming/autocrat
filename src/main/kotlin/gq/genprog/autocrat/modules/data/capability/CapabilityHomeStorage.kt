package gq.genprog.autocrat.modules.data.capability

import gq.genprog.autocrat.modules.data.IHomeCapability
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
object CapabilityHomeStorage {
    @CapabilityInject(IHomeCapability::class)
    var HOME_STORAGE_CAPABILITY: Capability<IHomeCapability>? = null
}
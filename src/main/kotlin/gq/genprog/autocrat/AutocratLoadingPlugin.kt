package gq.genprog.autocrat

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin

/**
 * Written by @offbeatwitch.
 * Licensed under MIT.
 */
@IFMLLoadingPlugin.TransformerExclusions("gq.genprog.autocrat", "kotlin")
@IFMLLoadingPlugin.MCVersion("1.12.2")
class AutocratLoadingPlugin: IFMLLoadingPlugin {
    override fun getModContainerClass(): String? {
        return null
    }

    override fun getASMTransformerClass(): Array<String> {
        return arrayOf("gq.genprog.autocrat.transformer.AutoClassTransformer")
    }

    override fun getSetupClass(): String? {
        return null
    }

    override fun injectData(data: MutableMap<String, Any>?) {
    }

    override fun getAccessTransformerClass(): String? {
        return null
    }
}
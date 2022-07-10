package com.github.eth0net.enchantmenu

import net.fabricmc.api.ModInitializer

@Suppress("UNUSED")
object EnchantMenu: ModInitializer {
    private const val MOD_ID = "enchant-menu"
    override fun onInitialize() {
        println("Enchant menu mod has been initialized.")
    }
}

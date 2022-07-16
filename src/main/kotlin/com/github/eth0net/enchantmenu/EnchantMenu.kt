package com.github.eth0net.enchantmenu

import com.github.eth0net.enchantmenu.screen.EnchantMenuScreenHandler
import net.fabricmc.api.ModInitializer
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Suppress("UNUSED")
object EnchantMenu : ModInitializer {
    private const val MOD_ID = "enchant-menu"

    internal val LOGGER: Logger = LogManager.getLogger(MOD_ID)

    internal val SCREEN_HANDLER_TYPE: ScreenHandlerType<EnchantMenuScreenHandler> =
        ScreenHandlerType { syncId, inventory -> EnchantMenuScreenHandler(syncId, inventory) }

    override fun onInitialize() {
        LOGGER.info("EnchantMenu initializing...")

        Registry.register(Registry.SCREEN_HANDLER, MOD_ID, SCREEN_HANDLER_TYPE)

        LOGGER.info("EnchantMenu initialized.")
    }
}

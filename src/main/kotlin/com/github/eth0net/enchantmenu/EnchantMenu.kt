package com.github.eth0net.enchantmenu

import com.github.eth0net.enchantmenu.config.EnchantMenuConfig
import com.github.eth0net.enchantmenu.network.channel.*
import com.github.eth0net.enchantmenu.screen.EnchantMenuScreenHandler
import com.github.eth0net.enchantmenu.screen.EnchantMenuScreenHandlerFactory
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager

@Suppress("UNUSED")
object EnchantMenu : ModInitializer {
    private const val MOD_ID = "enchant-menu"

    internal val config = EnchantMenuConfig()

    internal val log = LogManager.getLogger(MOD_ID)

    internal val SCREEN_HANDLER = Registry.register(
        Registry.SCREEN_HANDLER, id("enchant_menu"), ScreenHandlerType(::EnchantMenuScreenHandler)
    )

    override fun onInitialize() {
        log.info("EnchantMenu initializing...")

        ServerPlayNetworking.registerGlobalReceiver(MenuChannel) { _, player, _, _, _ ->
            player.openHandledScreen(EnchantMenuScreenHandlerFactory)
        }
        ServerPlayNetworking.registerGlobalReceiver(IncrementLevelChannel) { _, player, _, _, _ ->
            (player.currentScreenHandler as? EnchantMenuScreenHandler)?.incrementLevel()
        }
        ServerPlayNetworking.registerGlobalReceiver(DecrementLevelChannel) { _, player, _, _, _ ->
            (player.currentScreenHandler as? EnchantMenuScreenHandler)?.decrementLevel()
        }
        ServerPlayNetworking.registerGlobalReceiver(ToggleIncompatibleChannel) { _, player, _, _, _ ->
            (player.currentScreenHandler as? EnchantMenuScreenHandler)?.toggleIncompatible()
        }
        ServerPlayNetworking.registerGlobalReceiver(ToggleLevelChannel) { _, player, _, _, _ ->
            (player.currentScreenHandler as? EnchantMenuScreenHandler)?.toggleLevel()
        }
        ServerPlayNetworking.registerGlobalReceiver(ToggleTreasureChannel) { _, player, _, _, _ ->
            (player.currentScreenHandler as? EnchantMenuScreenHandler)?.toggleTreasure()
        }

        log.info("EnchantMenu initialized.")
    }

    internal fun id(path: String) = Identifier(MOD_ID, path)
}

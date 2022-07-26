package com.github.eth0net.enchantmenu

import com.github.eth0net.enchantmenu.network.channel.*
import com.github.eth0net.enchantmenu.screen.EnchantMenuScreenHandler
import com.github.eth0net.enchantmenu.screen.EnchantMenuScreenHandlerFactory
import com.github.eth0net.enchantmenu.util.Identifier
import com.github.eth0net.enchantmenu.util.Logger
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.registry.Registry

@Suppress("UNUSED")
object EnchantMenu : ModInitializer {
    internal val SCREEN_HANDLER = Registry.register(
        Registry.SCREEN_HANDLER, Identifier("enchant_menu"), ScreenHandlerType(::EnchantMenuScreenHandler)
    )

    override fun onInitialize() {
        Logger.info("EnchantMenu initializing...")

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

        Logger.info("EnchantMenu initialized.")
    }
}

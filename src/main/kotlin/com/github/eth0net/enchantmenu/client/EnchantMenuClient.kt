package com.github.eth0net.enchantmenu.client

import com.github.eth0net.enchantmenu.EnchantMenu
import com.github.eth0net.enchantmenu.client.gui.screen.EnchantMenuScreen
import com.github.eth0net.enchantmenu.client.keybinding.MenuKeyBinding
import com.github.eth0net.enchantmenu.network.channel.MenuChannel
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.gui.screen.ingame.HandledScreens

@Suppress("UNUSED")
object EnchantMenuClient : ClientModInitializer {
    override fun onInitializeClient() {
        EnchantMenu.log.info("EnchantMenuClient initializing...")

        HandledScreens.register(EnchantMenu.SCREEN_HANDLER, ::EnchantMenuScreen)

        ClientTickEvents.END_CLIENT_TICK.register {
            while (MenuKeyBinding.wasPressed()) ClientPlayNetworking.send(MenuChannel, PacketByteBufs.empty())
        }

        EnchantMenu.log.info("EnchantMenuClient initialized.")
    }
}

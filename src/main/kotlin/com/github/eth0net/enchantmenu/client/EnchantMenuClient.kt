package com.github.eth0net.enchantmenu.client

import com.github.eth0net.enchantmenu.EnchantMenu
import com.github.eth0net.enchantmenu.client.gui.screen.EnchantMenuScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

@Suppress("UNUSED")
object EnchantMenuClient : ClientModInitializer {
    private val openKey = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.enchant-menu.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, "category.enchant-menu.general"
        )
    )

    override fun onInitializeClient() {
        EnchantMenu.LOGGER.info("EnchantMenuClient initializing...")

        HandledScreens.register(EnchantMenu.SCREEN_HANDLER, ::EnchantMenuScreen)

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (openKey.wasPressed()) {
                client.player?.sendMessage(Text.literal("Enchant Menu Open"))
                ClientPlayNetworking.send(EnchantMenu.OPEN_PACKET, PacketByteBufs.empty())
            }
        }

        EnchantMenu.LOGGER.info("EnchantMenuClient initialized.")
    }
}

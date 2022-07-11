package com.github.eth0net.enchantmenu.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

@Suppress("UNUSED")
object EnchantMenuClient : ClientModInitializer {
    private val keyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.enchant-menu.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, "category.enchant-menu.general"
        )
    )

    override fun onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (keyBinding.wasPressed()) client.player?.sendMessage(Text.literal("Enchant Menu Open"))
        }
    }
}

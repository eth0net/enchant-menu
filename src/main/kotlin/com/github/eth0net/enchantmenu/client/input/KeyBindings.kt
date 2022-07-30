package com.github.eth0net.enchantmenu.client.input

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

internal object KeyBindings {
    internal val ToggleMenu = KeyBinding(
        "key.enchant-menu.menu",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_X,
        "category.enchant-menu.general"
    )

    internal val DecrementLevel = KeyBinding(
        "key.enchant-menu.decrement_level",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_MINUS,
        "category.enchant-menu.general"
    )

    internal val IncrementLevel = KeyBinding(
        "key.enchant-menu.increment_level",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_EQUAL,
        "category.enchant-menu.general"
    )

    internal val ToggleIncompatible = KeyBinding(
        "key.enchant-menu.toggle_incompatible",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_I,
        "category.enchant-menu.general"
    )

    internal val ToggleTreasure = KeyBinding(
        "key.enchant-menu.toggle_treasure",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_P,
        "category.enchant-menu.general"
    )

    internal val ToggleLevel = KeyBinding(
        "key.enchant-menu.toggle_level",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_O,
        "category.enchant-menu.general"
    )

    private val keysToRegister = listOf(
        ToggleMenu,
        DecrementLevel,
        IncrementLevel,
        ToggleIncompatible,
        ToggleLevel,
        ToggleTreasure
    )

    fun register() = keysToRegister.forEach { KeyBindingHelper.registerKeyBinding(it) }
}

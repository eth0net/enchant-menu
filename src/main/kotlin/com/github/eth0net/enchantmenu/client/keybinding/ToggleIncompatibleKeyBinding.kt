package com.github.eth0net.enchantmenu.client.keybinding

import com.github.eth0net.enchantmenu.client.keybinding.key.ToggleIncompatibleKey
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil

internal val ToggleIncompatibleKeyBinding = KeyBindingHelper.registerKeyBinding(
    KeyBinding(
        "key.enchant-menu.toggle_incompatible",
        InputUtil.Type.KEYSYM,
        ToggleIncompatibleKey,
        "category.enchant-menu.general"
    )
)

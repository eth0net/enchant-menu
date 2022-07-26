package com.github.eth0net.enchantmenu.client.keybinding

import com.github.eth0net.enchantmenu.client.keybinding.key.DecrementLevelKey
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil

internal val DecrementLevelKeyBinding = KeyBindingHelper.registerKeyBinding(
    KeyBinding(
        "key.enchant-menu.decrement_level", InputUtil.Type.KEYSYM, DecrementLevelKey, "category.enchant-menu.general"
    )
)

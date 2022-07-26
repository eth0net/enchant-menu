package com.github.eth0net.enchantmenu.client.keybinding

import com.github.eth0net.enchantmenu.client.keybinding.key.ToggleTreasureKey
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil

internal val ToggleTreasureKeyBinding = KeyBindingHelper.registerKeyBinding(
    KeyBinding(
        "key.enchant-menu.toggle_treasure", InputUtil.Type.KEYSYM, ToggleTreasureKey, "category.enchant-menu.general"
    )
)

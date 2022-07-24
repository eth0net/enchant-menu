package com.github.eth0net.enchantmenu.client.keybinding

import com.github.eth0net.enchantmenu.client.keybinding.keys.MenuKey
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil

internal val MenuKeyBinding = KeyBindingHelper.registerKeyBinding(
    KeyBinding(
        "key.enchant-menu.menu", InputUtil.Type.KEYSYM, MenuKey, "category.enchant-menu.general"
    )
)

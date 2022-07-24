package com.github.eth0net.enchantmenu.client.keybinding

import com.github.eth0net.enchantmenu.client.keybinding.keys.DecrementKey
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil

internal val DecrementKeyBinding = KeyBindingHelper.registerKeyBinding(
    KeyBinding(
        "key.enchant-menu.decrement", InputUtil.Type.KEYSYM, DecrementKey, "category.enchant-menu.general"
    )
)

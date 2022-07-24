package com.github.eth0net.enchantmenu.screen.slot

import com.mojang.datafixers.util.Pair
import net.minecraft.inventory.Inventory
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.util.Identifier

class OffhandSlot(inventory: Inventory, index: Int, x: Int, y: Int) : Slot(inventory, index, x, y) {
    override fun getBackgroundSprite(): Pair<Identifier, Identifier> {
        return Pair(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, PlayerScreenHandler.EMPTY_OFFHAND_ARMOR_SLOT)
    }
}

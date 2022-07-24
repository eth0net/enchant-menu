package com.github.eth0net.enchantmenu.screen.slot

import com.mojang.datafixers.util.Pair
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.util.Identifier

class ArmorSlot(inventory: PlayerInventory, index: Int, x: Int, y: Int) : Slot(inventory, index, x, y) {
    private val textures = arrayOf<Identifier>(
        PlayerScreenHandler.EMPTY_BOOTS_SLOT_TEXTURE,
        PlayerScreenHandler.EMPTY_LEGGINGS_SLOT_TEXTURE,
        PlayerScreenHandler.EMPTY_CHESTPLATE_SLOT_TEXTURE,
        PlayerScreenHandler.EMPTY_HELMET_SLOT_TEXTURE
    )

    private val armorSlots = arrayOf(
        EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD
    )

    private val slotType get() = armorSlots[index-36]

    private val isEmpty get() = stack.isEmpty
    private val hasBindingCurse get() = EnchantmentHelper.hasBindingCurse(stack)

    override fun canInsert(stack: ItemStack) = MobEntity.getPreferredEquipmentSlot(stack) == slotType

    override fun canTakeItems(playerEntity: PlayerEntity): Boolean {
        if (!isEmpty && hasBindingCurse && !playerEntity.isCreative) return false
        return super.canTakeItems(playerEntity)
    }

    override fun getBackgroundSprite(): Pair<Identifier, Identifier> {
        return Pair(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, textures[index-36])
    }

    override fun getMaxItemCount() = 1
}

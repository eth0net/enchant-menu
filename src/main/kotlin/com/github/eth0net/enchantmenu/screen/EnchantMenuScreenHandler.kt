package com.github.eth0net.enchantmenu.screen

import com.github.eth0net.enchantmenu.EnchantMenu
import com.github.eth0net.enchantmenu.screen.slot.ArmorSlot
import com.github.eth0net.enchantmenu.screen.slot.EnchantSlot
import com.github.eth0net.enchantmenu.screen.slot.OffhandSlot
import com.github.eth0net.enchantmenu.util.Logger
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.Slot
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.registry.Registry

class EnchantMenuScreenHandler(
    syncId: Int, playerInventory: PlayerInventory, private val context: ScreenHandlerContext
) : ScreenHandler(EnchantMenu.SCREEN_HANDLER, syncId) {
    constructor(syncId: Int, playerInventory: PlayerInventory) : this(
        syncId, playerInventory, ScreenHandlerContext.EMPTY
    )

    private val handler = this

    private var inventory: Inventory = object : SimpleInventory(1) {
        override fun markDirty() {
            super.markDirty()
            handler.onContentChanged(this)
        }
    }

    internal val minLevel = 1
    internal val maxLevel = 10
    internal var level = minLevel
        set(value) {
            field = if (value < minLevel) {
                minLevel
            } else if (value > maxLevel) {
                maxLevel
            } else {
                value
            }
        }

    internal var enchantments: List<Triple<Enchantment, Int, Boolean>> = listOf()

    internal var incompatibleUnlocked = false
    internal var levelUnlocked = false
    internal var treasureUnlocked = false

    init {
        addSlot(EnchantSlot(inventory, 0, 29, 41))
        playerInventory.main.forEachIndexed { index, _ ->
            val x = 29 + index % 9 * 18
            val y = 85 + if (index < 9) 58 else (index - 9) / 9 * 18
            addSlot(Slot(playerInventory, index, x, y))
        }
        playerInventory.armor.forEachIndexed { index, _ ->
            addSlot(ArmorSlot(playerInventory, index + 36, 7, 67 + (3 - index) * 18))
        }
        addSlot(OffhandSlot(playerInventory, 40, 7, 142))
    }

    override fun onButtonClick(player: PlayerEntity, id: Int): Boolean {
        if (id !in enchantments.indices) {
            Logger.error("${player.name} tried to press invalid button $id")
            return false
        }

        val (enchantment, oldLevel, compatible) = enchantments[id]
        if (!compatible && !incompatibleUnlocked) return false

        val oldStack = inventory.getStack(0)
        if (oldStack.isEmpty) return false

        var newStack = oldStack
        if (newStack.isOf(Items.BOOK)) {
            newStack = ItemStack(Items.ENCHANTED_BOOK)
            newStack.nbt = oldStack.nbt?.copy()
            inventory.setStack(0, newStack)
        }

        if (oldLevel > 0) {
            newStack.removeEnchantment(enchantment)
        } else {
            newStack.addEnchantment(enchantment, level)
        }

        inventory.markDirty()
        onContentChanged(inventory)

        context.run { world, pos ->
            world.playSound(
                null,
                pos,
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                SoundCategory.BLOCKS,
                1.0F,
                world.random.nextFloat() * 0.1F + 0.9F
            )
        }

        return true
    }

    override fun onContentChanged(inventory: Inventory) {
        if (inventory != this.inventory) return
        enchantments = listOf()
        val stack = inventory.getStack(0)
        if (!stack.isEmpty) enchantments = stack.acceptableEnchantments.map { stack.enchantmentEntry(it) }
        sendContentUpdates()
    }

    override fun canUse(player: PlayerEntity) = inventory.canPlayerUse(player)

    override fun close(player: PlayerEntity) {
        super.close(player)
        dropInventory(player, inventory)
    }

    override fun transferSlot(player: PlayerEntity, index: Int): ItemStack {
        val slot = slots[index]
        if (!slot.hasStack()) return ItemStack.EMPTY

        val stack = slot.stack
        val stack2 = stack.copy()

        if (index == 0) {
            // transfer to the first valid slot in the player inventory
            if (!insertItem(stack2, 1, 37, true)) return ItemStack.EMPTY
        } else {
            // transfer to the enchant menu slot
            if (slots[0].hasStack() || !slots[0].canInsert(stack2)) return ItemStack.EMPTY

            val stack3 = stack2.copy()
            stack3.count = 1
            stack2.decrement(1)
            slots[0].stack = stack3
        }

        if (stack2.isEmpty) {
            slot.stack = ItemStack.EMPTY
        } else {
            slot.markDirty()
        }

        if (stack2.count == stack.count) return ItemStack.EMPTY

        slot.onTakeItem(player, stack2)

        return stack
    }

    private val ItemStack.acceptableEnchantments get() = Registry.ENCHANTMENT.filter { it.isAcceptableItem(this) && (!it.isTreasure || treasureUnlocked) }

    private fun ItemStack.enchantmentCompatible(enchantment: Enchantment): Boolean {
        return EnchantmentHelper.fromNbt(enchantments).all { it.key.canCombine(enchantment) }
    }

    private fun ItemStack.enchantmentLevel(enchantment: Enchantment): Int {
        return EnchantmentHelper.getLevel(enchantment, this)
    }

    private fun ItemStack.enchantmentEntry(enchantment: Enchantment): Triple<Enchantment, Int, Boolean> {
        return Triple(enchantment, enchantmentLevel(enchantment), enchantmentCompatible(enchantment))
    }

    private fun ItemStack.removeEnchantment(enchantment: Enchantment) {
        EnchantmentHelper.set(EnchantmentHelper.fromNbt(enchantments).filter { it.key != enchantment }, this)
    }

    internal fun incrementLevel() {
        if (level < maxLevel) ++level
    }

    internal fun decrementLevel() {
        if (level > minLevel) --level
    }

    internal fun toggleIncompatible() {
        incompatibleUnlocked = !incompatibleUnlocked
    }

    internal fun toggleLevel() {
        levelUnlocked = !levelUnlocked
    }

    internal fun toggleTreasure() {
        treasureUnlocked = !treasureUnlocked
    }
}

package com.github.eth0net.enchantmenu.screen

import com.github.eth0net.enchantmenu.EnchantMenu
import net.minecraft.advancement.criterion.Criteria
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
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.stat.Stats
import net.minecraft.util.registry.Registry

class EnchantMenuScreenHandler(
    syncId: Int, playerInventory: PlayerInventory, private val context: ScreenHandlerContext
) : ScreenHandler(EnchantMenu.SCREEN_HANDLER, syncId) {
    constructor(syncId: Int, playerInventory: PlayerInventory) : this(
        syncId, playerInventory, ScreenHandlerContext.EMPTY
    )

    private val handler = this

    internal var enchantments: List<Enchantment> = listOf()
    private var inventory: Inventory = object : SimpleInventory(1) {
        override fun markDirty() {
            super.markDirty()
            handler.onContentChanged(this)
        }
    }

    private val minLevel = 1
    private val maxLevel = 10
    internal var level = 1

    internal fun incLevel() {
        if (level < maxLevel) level = level.inc()
    }

    internal fun decLevel() {
        if (level > minLevel) level = level.dec()
    }

    init {
        addSlot(object : Slot(inventory, 0, 15, 47) {
            override fun canInsert(stack: ItemStack) = true

            override fun getMaxItemCount() = 1
        })
        playerInventory.main.forEachIndexed { index, _ ->
            val x = 8 + index % 9 * 18
            val y = 84 + if (index < 9) 58 else (index - 9) / 9 * 18
            addSlot(Slot(playerInventory, index, x, y))
        }
    }

    override fun onContentChanged(inventory: Inventory) {
        if (inventory != this.inventory) return
        enchantments = inventory.getStack(0).acceptableEnchantments
        this.sendContentUpdates()
    }

    override fun onButtonClick(player: PlayerEntity, id: Int): Boolean {
        if (id < 0 || id >= enchantments.size) {
            EnchantMenu.LOGGER.error("${player.name} tried to press invalid enchant button $id")
            return false
        }

        val oldStack = inventory.getStack(0)
        if (oldStack.isEmpty) return false

        var newStack = oldStack
        if (newStack.isOf(Items.BOOK)) {
            newStack = ItemStack(Items.ENCHANTED_BOOK)
            newStack.nbt = oldStack.nbt?.copy()
            inventory.setStack(0, newStack)
        }

        val enchantment = enchantments[id]
        if (oldStack.hasEnchantment(enchantment)) {
            newStack.removeEnchantment(enchantment)
        } else {
            newStack.addEnchantment(enchantment, level)
        }

        player.incrementStat(Stats.ENCHANT_ITEM)
        if (player is ServerPlayerEntity) Criteria.ENCHANTED_ITEM.trigger(player, newStack, level)

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

    private fun ItemStack.hasEnchantment(enchantment: Enchantment) = EnchantmentHelper.getLevel(enchantment, this) > 0

    private fun ItemStack.removeEnchantment(enchantment: Enchantment) {
        EnchantmentHelper.set(EnchantmentHelper.fromNbt(enchantments).filter { it.key != enchantment }, this)
    }

    private val ItemStack.acceptableEnchantments: List<Enchantment>
        get() = Registry.ENCHANTMENT.filter { it.isAcceptableItem(this) }

    override fun close(player: PlayerEntity) {
        super.close(player)
        dropInventory(player, inventory)
    }

    override fun canUse(player: PlayerEntity) = true

    override fun transferSlot(player: PlayerEntity, index: Int): ItemStack {
        val slot = slots[index]

        if (!slot.hasStack()) return ItemStack.EMPTY

        val stack = slot.stack
        val stack2 = stack.copy()

        if (index == 0) {
            if (!insertItem(stack2, 1, 37, true)) return ItemStack.EMPTY
        } else {
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
}

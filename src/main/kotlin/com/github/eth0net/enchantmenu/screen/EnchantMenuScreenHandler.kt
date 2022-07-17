package com.github.eth0net.enchantmenu.screen

import com.github.eth0net.enchantmenu.EnchantMenu
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentLevelEntry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.EnchantedBookItem
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
    internal val level: Int = 1

    internal var enchantments: List<Enchantment>? = null
    private var inventory: Inventory = object : SimpleInventory(1) {
        override fun markDirty() {
            super.markDirty()
            handler.onContentChanged(this)
        }
    }

    init {
        addSlot(object : Slot(inventory, 0, 15, 47) {
            override fun canInsert(stack: ItemStack) = true

            override fun getMaxItemCount() = 1
        })
        for (i in playerInventory.main.indices) {
            addSlot(Slot(playerInventory, i, 8 + i % 9 * 18, 8 + i / 9 * 18))
        }
    }

    override fun onContentChanged(inventory: Inventory) {
        if (inventory != this.inventory) return

        val itemStack = inventory.getStack(0)
        if (itemStack.isEmpty || !itemStack.isEnchantable) return

        enchantments = generateEnchantments(itemStack)
        this.sendContentUpdates()
    }

    override fun onButtonClick(player: PlayerEntity, id: Int): Boolean {
        val stack = inventory.getStack(0)
        if (stack.isEmpty) return false

        var stack2 = stack
        val list = generateEnchantments(stack)
        if (list.isEmpty()) return false

        val book = stack.isOf(Items.BOOK)
        if (book) {
            stack2 = ItemStack(Items.ENCHANTED_BOOK)
            stack2.nbt = stack.nbt!!.copy()
            inventory.setStack(0, stack2)
        }

        for (enchantment in list) {
            if (book) {
                EnchantedBookItem.addEnchantment(stack2, EnchantmentLevelEntry(enchantment, level))
            } else {
                stack2.addEnchantment(enchantment, level)
            }
        }

        player.incrementStat(Stats.ENCHANT_ITEM)
        if (player is ServerPlayerEntity) Criteria.ENCHANTED_ITEM.trigger(player, stack2, level)

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

    private fun generateEnchantments(stack: ItemStack) = Registry.ENCHANTMENT.filter { it.isAcceptableItem(stack) }

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

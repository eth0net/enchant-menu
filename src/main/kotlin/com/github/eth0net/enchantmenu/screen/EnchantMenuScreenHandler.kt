package com.github.eth0net.enchantmenu.screen

import com.github.eth0net.enchantmenu.EnchantMenu
import com.github.eth0net.enchantmenu.config.EnchantMenuConfig
import com.github.eth0net.enchantmenu.screen.slot.ArmorSlot
import com.github.eth0net.enchantmenu.screen.slot.EnchantSlot
import com.github.eth0net.enchantmenu.screen.slot.OffhandSlot
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
import net.minecraft.text.Text
import net.minecraft.registry.Registries

class EnchantMenuScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    private val context: ScreenHandlerContext
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

    internal var enchantments: List<Triple<Enchantment, Int, Boolean>> = listOf()

    internal var search = ""
        set(value) {
            field = value
            onContentChanged(inventory)
        }

    internal var level = EnchantMenuConfig.Levels.default
        set(value) {
            field = value.coerceIn(
                EnchantMenuConfig.Levels.minimum,
                EnchantMenuConfig.Levels.maximum
            )
        }

    internal var checkPermission = EnchantMenuConfig.checkPermission
        get() = EnchantMenuConfig.checkPermission && field

    internal var incompatibleUnlocked = EnchantMenuConfig.DefaultLimitBreaks.incompatible
        get() = EnchantMenuConfig.AllowLimitBreaks.incompatible && field

    internal var levelUnlocked = EnchantMenuConfig.DefaultLimitBreaks.level
        get() = EnchantMenuConfig.AllowLimitBreaks.level && field

    internal var treasureUnlocked = EnchantMenuConfig.DefaultLimitBreaks.treasure
        get() = EnchantMenuConfig.AllowLimitBreaks.treasure && field

    init {
        addSlot(EnchantSlot(inventory, 0, 15, 40))
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
        if (!player.canEnchant()) {
            player.sendMessage(Text.translatable("error.enchant-menu.permission"))
            return false
        }

        if (id !in enchantments.indices) {
            EnchantMenu.log.error("${player.name} tried to press invalid button $id")
            return false
        }

        val (enchantment, oldLevel, compatible) = enchantments[id]
        val hasEnchantment = oldLevel > 0
        if (!compatible && !incompatibleUnlocked && !hasEnchantment) return false

        val oldStack = inventory.getStack(0)
        if (oldStack.isEmpty) return false

        var newStack = oldStack
        if (newStack.isOf(Items.BOOK)) {
            newStack = ItemStack(Items.ENCHANTED_BOOK)
            oldStack.nbt?.let { newStack.nbt = it.copy() }
            inventory.setStack(0, newStack)
        }

        if (hasEnchantment) {
            val enchantments = EnchantmentHelper.get(newStack)
                .filter { (e, _) -> e != enchantment }
                .toMap()
            EnchantmentHelper.set(enchantments, newStack)
        } else {
            val lvl = if (level > enchantment.maxLevel && !levelUnlocked) enchantment.maxLevel else level
            newStack.addEnchantment(enchantment, lvl)
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
        if (!stack.isEmpty) {
            enchantments = acceptableEnchantments(stack).map { enchantment ->
                Triple(
                    enchantment,
                    EnchantmentHelper.getLevel(enchantment, stack),
                    enchantmentCompatible(stack, enchantment)
                )
            }
        }
        sendContentUpdates()
    }

    override fun canUse(player: PlayerEntity) = inventory.canPlayerUse(player)

    override fun onClosed(player: PlayerEntity) {
        super.onClosed(player)
        dropInventory(player, inventory)
    }

    override fun quickMove(player: PlayerEntity, index: Int): ItemStack {
        val slot = slots[index]
        if (!slot.hasStack()) return ItemStack.EMPTY

        val stack = slot.stack
        val originalStack = stack.copy()

        if (index == 0) {
            if (!insertItem(stack, 1, 37, true)) {
                return ItemStack.EMPTY
            }
        } else {
            if (slots[0].hasStack() || !slots[0].canInsert(stack)) {
                return ItemStack.EMPTY
            }

            val singleItem = stack.copy()
            singleItem.count = 1
            stack.decrement(1)
            slots[0].stack = singleItem
        }

        if (stack.isEmpty) {
            slot.stack = ItemStack.EMPTY
        } else {
            slot.markDirty()
        }

        if (stack.count == originalStack.count) {
            return ItemStack.EMPTY
        }

        slot.onTakeItem(player, stack)
        return originalStack
    }

    private fun PlayerEntity.canEnchant() = !checkPermission || hasPermissionLevel(2)

    private fun acceptableEnchantments(stack: ItemStack): List<Enchantment> {
        return Registries.ENCHANTMENT
            .stream()                                 // Stream<Enchantment>
            .filter { enchantment ->
                val allowed = !enchantment.isTreasure || treasureUnlocked || stack.hasEnchantment(enchantment)
                val inSearch = enchantment.getName(level).string
                    .lowercase()
                    .contains(search.lowercase())
                enchantment.isAcceptableItem(stack)
                        && allowed
                        && inSearch
            }
            .toList()
    }


    private fun enchantmentCompatible(stack: ItemStack, enchantment: Enchantment): Boolean {
        return EnchantmentHelper.get(stack).all { (e, _) -> e.canCombine(enchantment) }
    }

    private fun ItemStack.enchantmentLevel(enchantment: Enchantment) =
        EnchantmentHelper.getLevel(enchantment, this)

    private fun ItemStack.hasEnchantment(enchantment: Enchantment) =
        enchantmentLevel(enchantment) > 0

    internal fun incrementLevel() {
        if (level < EnchantMenuConfig.Levels.maximum) ++level
        onContentChanged(inventory)
    }

    internal fun decrementLevel() {
        if (level > EnchantMenuConfig.Levels.minimum) --level
        onContentChanged(inventory)
    }

    internal fun toggleIncompatible() {
        incompatibleUnlocked = !incompatibleUnlocked
    }

    internal fun toggleLevel() {
        levelUnlocked = !levelUnlocked
    }

    internal fun toggleTreasure() {
        treasureUnlocked = !treasureUnlocked
        onContentChanged(inventory)
    }
}
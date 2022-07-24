package com.github.eth0net.enchantmenu.screen

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.text.MutableText
import net.minecraft.text.Text

object EnchantMenuScreenHandlerFactory : NamedScreenHandlerFactory {
    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): EnchantMenuScreenHandler {
        return EnchantMenuScreenHandler(syncId, inv)
    }

    override fun getDisplayName(): MutableText = Text.translatable("enchant-menu.title")
}

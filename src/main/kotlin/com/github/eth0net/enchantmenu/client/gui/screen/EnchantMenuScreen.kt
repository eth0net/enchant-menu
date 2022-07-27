package com.github.eth0net.enchantmenu.client.gui.screen

import com.github.eth0net.enchantmenu.EnchantMenu
import com.github.eth0net.enchantmenu.client.keybinding.*
import com.github.eth0net.enchantmenu.network.channel.*
import com.github.eth0net.enchantmenu.screen.EnchantMenuScreenHandler
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.TexturedButtonWidget
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import kotlin.math.roundToInt

@Environment(EnvType.CLIENT)
class EnchantMenuScreen(handler: EnchantMenuScreenHandler, playerInventory: PlayerInventory, title: Text) :
    HandledScreen<EnchantMenuScreenHandler>(handler, object : PlayerInventory(playerInventory.player) {
        override fun getDisplayName() = Text.empty()
    }, title) {

    private val texture = EnchantMenu.id("textures/gui/enchant_menu.png")
    private var stack = ItemStack.EMPTY
    private var ticks = 0

    private val maxRows = 5
    private val canScroll get() = handler.enchantments.size > maxRows
    private val maxScrollOffset get() = if (canScroll) handler.enchantments.size - maxRows else 0
    private var scrollOffset = 0
        set(value) {
            field = if (value <= 0) {
                0
            } else if (value > maxScrollOffset) {
                maxScrollOffset
            } else {
                value
            }
        }

    init {
        backgroundWidth = 196
        backgroundHeight = 166
    }

    override fun handledScreenTick() {
        super.handledScreenTick()
        val itemStack = handler.slots[0].stack
        if (itemStack != stack) stack = itemStack
        ++ticks
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val x = (width - backgroundWidth) / 2
        val y = (height - backgroundHeight) / 2
        val clickX = mouseX.roundToInt() - (x + 44)

        for (i in 0 until maxRows) {
            val index = i + scrollOffset
            val clickY = mouseY.roundToInt() - (y + 19 + 12 * i)
            val inBounds = clickX in 0..140 && clickY in 0..11
            if (!inBounds || !handler.onButtonClick(client!!.player as PlayerEntity, index)) continue
            client!!.interactionManager!!.clickButton(handler.syncId, index)
            return true
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        scrollOffset -= amount.roundToInt()
        return super.mouseScrolled(mouseX, mouseY, amount)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (MenuKeyBinding.matchesKey(keyCode, scanCode)) close()
        if (IncrementLevelKeyBinding.matchesKey(keyCode, scanCode)) onIncrementLevelClick()
        if (DecrementLevelKeyBinding.matchesKey(keyCode, scanCode)) onDecrementLevelClick()
        if (ToggleIncompatibleKeyBinding.matchesKey(keyCode, scanCode)) onToggleIncompatibleClick()
        if (ToggleLevelKeyBinding.matchesKey(keyCode, scanCode)) onToggleLevelClick()
        if (ToggleTreasureKeyBinding.matchesKey(keyCode, scanCode)) onToggleTreasureClick()
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        DiffuseLighting.disableGuiDepthLighting()
        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.setShaderTexture(0, texture)
        val x = (width - backgroundWidth) / 2
        val y = (height - backgroundHeight) / 2
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight)
        DiffuseLighting.enableGuiDepthLighting()
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        zOffset = 0

        // level text
        val level = handler.level.toString()
        textRenderer.drawTrimmed(Text.literal(level), x + 23 - level.length * 3, y + 21, level.length * 6, 0xFFFFFF)

        // level change buttons
        clearChildren()
        if (handler.level < handler.maxLevel) addDrawableChild(TexturedButtonWidget(
            x + 31, y + 18, 8, 13, 149, 166, texture
        ) { onIncrementLevelClick() })
        if (handler.level > handler.minLevel) addDrawableChild(TexturedButtonWidget(
            x + 6, y + 18, 8, 13, 141, 166, texture
        ) { onDecrementLevelClick() })

        // scroll marker
        if (canScroll) {
            val scrollMarkerX = x + 185
            val scrollMarkerY = y + 19 + (48 * (scrollOffset.toFloat() / maxScrollOffset)).toInt()
            RenderSystem.setShaderTexture(0, texture)
            drawTexture(matrices, scrollMarkerX, scrollMarkerY, 141, 205, 4, 12)
        }

        // limit breaks
        addDrawableChild(TexturedButtonWidget(
            x + 152, y + 5, 11, 11, 168, if (handler.incompatibleUnlocked) 177 else 166, texture
        ) { onToggleIncompatibleClick() })
        addDrawableChild(TexturedButtonWidget(
            x + 165, y + 5, 11, 11, 179, if (handler.levelUnlocked) 177 else 166, texture
        ) { onToggleLevelClick() })
        addDrawableChild(TexturedButtonWidget(
            x + 178, y + 5, 11, 11, 190, if (handler.treasureUnlocked) 177 else 166, texture
        ) { onToggleTreasureClick() })

        // enchantments list, from scroll offset to max rows
        for (i in 0 until maxRows) {
            val index = i + scrollOffset
            if (index >= handler.enchantments.size) break

            val xOffset = x + 44
            val yOffset = y + 19 + 12 * i

            val (enchantment, currentLevel, compatible) = handler.enchantments[index]
            val hasEnchantment = currentLevel > 0
            var color = 0xFFFF80

            RenderSystem.setShaderTexture(0, texture)

            val hoverX = mouseX - xOffset
            val hoverY = mouseY - yOffset
            if (!compatible && !handler.incompatibleUnlocked && !hasEnchantment) {
                color = 37373737
                drawTexture(matrices, xOffset, yOffset, 0, 178, 141, 12)
            } else if (hoverX in 0 until 141 && hoverY in 0 until 12) {
                drawTexture(matrices, xOffset, yOffset, 0, 202, 141, 12)
            } else if (hasEnchantment) {
                drawTexture(matrices, xOffset, yOffset, 0, 190, 141, 12)
            } else {
                drawTexture(matrices, xOffset, yOffset, 0, 166, 141, 12)
                color = 6839882
            }

            val overLimit = handler.level > enchantment.maxLevel && !handler.levelUnlocked
            val lvl = if (hasEnchantment) currentLevel else if (overLimit) enchantment.maxLevel else handler.level
            textRenderer.drawTrimmed(enchantment.getName(lvl), xOffset + 2, yOffset + 2, 137, color)
        }
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, client!!.tickDelta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
    }

    private fun onIncrementLevelClick() {
        handler.incrementLevel()
        ClientPlayNetworking.send(IncrementLevelChannel, PacketByteBufs.empty())
    }

    private fun onDecrementLevelClick() {
        handler.decrementLevel()
        ClientPlayNetworking.send(DecrementLevelChannel, PacketByteBufs.empty())
    }

    private fun onToggleIncompatibleClick() {
        handler.toggleIncompatible()
        ClientPlayNetworking.send(ToggleIncompatibleChannel, PacketByteBufs.empty())
    }

    private fun onToggleLevelClick() {
        handler.toggleLevel()
        ClientPlayNetworking.send(ToggleLevelChannel, PacketByteBufs.empty())
    }

    private fun onToggleTreasureClick() {
        handler.toggleTreasure()
        ClientPlayNetworking.send(ToggleTreasureChannel, PacketByteBufs.empty())
    }
}

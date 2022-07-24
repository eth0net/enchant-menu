package com.github.eth0net.enchantmenu.client.gui.screen

import com.github.eth0net.enchantmenu.client.keybinding.DecrementKeyBinding
import com.github.eth0net.enchantmenu.client.keybinding.IncrementKeyBinding
import com.github.eth0net.enchantmenu.client.keybinding.MenuKeyBinding
import com.github.eth0net.enchantmenu.network.channel.DecrementChannel
import com.github.eth0net.enchantmenu.network.channel.IncrementChannel
import com.github.eth0net.enchantmenu.screen.EnchantMenuScreenHandler
import com.github.eth0net.enchantmenu.util.Identifier
import com.github.eth0net.enchantmenu.util.Logger
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.TexturedButtonWidget
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.math.Matrix4f
import net.minecraft.util.math.Vec3f
import kotlin.math.roundToInt

@Environment(EnvType.CLIENT)
class EnchantMenuScreen(handler: EnchantMenuScreenHandler, playerInventory: PlayerInventory, title: Text) :
    HandledScreen<EnchantMenuScreenHandler>(handler, object : PlayerInventory(playerInventory.player) {
        override fun getDisplayName() = Text.empty()
    }, title) {

    private val texture = Identifier("textures/menu.png")
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
        val clickX = mouseX.roundToInt() - (x + 51)

        for (i in 0 until maxRows) {
            val index = i + scrollOffset
            val clickY = mouseY.roundToInt() - (y + 19 + 12 * i)
            val inBounds = clickX in 0..133 && clickY in 0..11
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
        if (IncrementKeyBinding.matchesKey(keyCode, scanCode)) incrementLevel()
        if (DecrementKeyBinding.matchesKey(keyCode, scanCode)) decrementLevel()
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
        textRenderer.drawTrimmed(Text.literal(level), x + 27 - level.length * 3, y + 21, level.length * 6, 0xFFFFFF)

        // level change buttons
        clearChildren()
        if (handler.level < handler.maxLevel) addDrawableChild(TexturedButtonWidget(
            x + 38, y + 18, 8, 13, 142, 166, texture
        ) { incrementLevel() })
        if (handler.level > handler.minLevel) addDrawableChild(TexturedButtonWidget(
            x + 7, y + 18, 8, 13, 134, 166, texture
        ) { decrementLevel() })

        // scroll marker
        if (canScroll) {
            val scrollMarkerX = x + 185
            val scrollMarkerY = y + 19 + (48 * (scrollOffset.toFloat() / maxScrollOffset)).toInt()
            RenderSystem.setShaderTexture(0, texture)
            drawTexture(matrices, scrollMarkerX, scrollMarkerY, 150, 166, 4, 12)
        }

        // enchantments list, from scroll offset to max rows
        for (i in 0 until maxRows) {
            val index = i + scrollOffset
            if (index >= handler.enchantments.size) break

            val xOffset = x + 51
            val yOffset = y + 19 + 12 * i

            val (enchantment, currentLevel) = handler.enchantments[index]
            val hasEnchantment = currentLevel > 0
            val text = enchantment.getName(if (hasEnchantment) currentLevel else handler.level)
            var color = 6839882

            RenderSystem.setShaderTexture(0, texture)

            val hoverX = mouseX - xOffset
            val hoverY = mouseY - yOffset
            if (hoverX in 0..137 && hoverY in 0..12) {
                drawTexture(matrices, xOffset, yOffset, 0, 202, 134, 12, )
                color = 0xFFFF80
            } else if (hasEnchantment) {
                drawTexture(matrices, xOffset, yOffset, 0, 190, 134, 12)
                color = 0xFFFF80
            } else {
                drawTexture(matrices, xOffset, yOffset, 0, 166, 134, 12)
            }

            textRenderer.drawTrimmed(text, xOffset + 2, yOffset + 2, 130, color)
        }
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, client!!.tickDelta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
    }

    private fun incrementLevel() {
        handler.incrementLevel()
        ClientPlayNetworking.send(IncrementChannel, PacketByteBufs.empty())
    }

    private fun decrementLevel() {
        handler.decrementLevel()
        ClientPlayNetworking.send(DecrementChannel, PacketByteBufs.empty())
    }
}

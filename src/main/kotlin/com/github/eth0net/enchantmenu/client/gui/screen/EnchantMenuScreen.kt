package com.github.eth0net.enchantmenu.client.gui.screen

import com.github.eth0net.enchantmenu.EnchantMenu
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
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.math.Matrix4f
import net.minecraft.util.math.Vec3f
import org.lwjgl.glfw.GLFW

@Environment(EnvType.CLIENT)
class EnchantMenuScreen(handler: EnchantMenuScreenHandler, playerInventory: PlayerInventory, title: Text) :
    HandledScreen<EnchantMenuScreenHandler>(handler, object : PlayerInventory(playerInventory.player) {
        override fun getDisplayName() = Text.empty()
    }, title) {

    private val texture = EnchantMenu.id("textures/menu.png")
    private var stack = ItemStack.EMPTY
    private var ticks = 0

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
        val clickX = mouseX - (x + 51).toDouble()

        for (i in handler.enchantments.indices) {
            val clickY = mouseY - (y + 7 + 12 * i).toDouble()
            val inBounds = clickX >= 0 && clickX < 138 && clickY >= 0 && clickY < 12
            if (!inBounds || !handler.onButtonClick(client!!.player as PlayerEntity, i)) continue
            client!!.interactionManager!!.clickButton(handler.syncId, i)
            return true
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_BRACKET) incrementLevel()
        if (keyCode == GLFW.GLFW_KEY_LEFT_BRACKET) decrementLevel()
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
        val scale = client!!.window.scaleFactor.toInt()
        RenderSystem.viewport((width - 320) / 2 * scale, (height - 240) / 2 * scale, 320 * scale, 240 * scale)
        val matrix4f = Matrix4f.translate(-0.34f, 0.23f, 0.0f)
        matrix4f.multiply(Matrix4f.viewboxMatrix(90.0, 1.3333334f, 9.0f, 80.0f))
        RenderSystem.backupProjectionMatrix()
        RenderSystem.setProjectionMatrix(matrix4f)
        matrices.push()
        val entry = matrices.peek()
        entry.positionMatrix.loadIdentity()
        entry.normalMatrix.loadIdentity()
        matrices.translate(0.0, 3.3, 1984.0)
        matrices.scale(5.0f, 5.0f, 5.0f)
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0f))
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(20.0f))
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180.0f))

        VertexConsumerProvider.immediate(Tessellator.getInstance().buffer).draw()
        matrices.pop()
        RenderSystem.viewport(0, 0, client!!.window.framebufferWidth, client!!.window.framebufferHeight)
        RenderSystem.restoreProjectionMatrix()
        DiffuseLighting.enableGuiDepthLighting()
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)

        // level text
        val level = handler.level.toString()
        textRenderer.drawTrimmed(Text.literal(level), x + 27 - level.length * 3, y + 21, level.length * 6, 0xFFFFFF)

        // level change buttons
        clearChildren()
        if (handler.level < handler.maxLevel) addDrawableChild(TexturedButtonWidget(
            x + 38, y + 18, 8, 13, 146, 166, texture
        ) { incrementLevel() })
        if (handler.level > handler.minLevel) addDrawableChild(TexturedButtonWidget(
            x + 7, y + 18, 8, 13, 138, 166, texture
        ) { decrementLevel() })

        // enchantments list
        handler.enchantments.forEachIndexed { index, (enchantment, currentLevel) ->
            val xOffset = x + 51
            val yOffset = y + 7 + 12 * index
            zOffset = 0
            RenderSystem.setShader(GameRenderer::getPositionTexShader)
            RenderSystem.setShaderTexture(0, texture)
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)

            val hasEnchantment = currentLevel > 0
            val text = enchantment.getName(if (hasEnchantment) currentLevel else handler.level)
            var color = 6839882

            val hoverX = mouseX - xOffset
            val hoverY = mouseY - yOffset
            if (hoverX in 0..137 && hoverY in 0..12) {
                drawTexture(matrices, xOffset, yOffset, 0, 202, 138, 12)
                color = 0xFFFF80
            } else if (hasEnchantment) {
                drawTexture(matrices, xOffset, yOffset, 0, 190, 138, 12)
                color = 0xFFFF80
            } else {
                drawTexture(matrices, xOffset, yOffset, 0, 166, 138, 12)
            }

            textRenderer.drawTrimmed(text, xOffset + 2, yOffset + 2, 134, color)
        }
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, client!!.tickDelta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)

//        handler.enchantments.run run@{
//            this.forEachIndexed loop@{ i, enchantment ->
//                if (!isPointWithinBounds(60, 14 + 19 * i, 108, 17, mouseX.toDouble(), mouseY.toDouble())) return@loop
//
//                val nameText = Text.translatable("container.enchant.clue", enchantment.getName(handler.level))
//                val list = listOf(nameText.formatted(Formatting.WHITE))
//
//                renderTooltip(matrices, list, mouseX, mouseY)
//                return@run
//            }
//        }
    }

    private fun incrementLevel() {
        handler.incrementLevel()
        ClientPlayNetworking.send(EnchantMenu.INC_PACKET, PacketByteBufs.empty())
    }

    private fun decrementLevel() {
        handler.decrementLevel()
        ClientPlayNetworking.send(EnchantMenu.DEC_PACKET, PacketByteBufs.empty())
    }
}

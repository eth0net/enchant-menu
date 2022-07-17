package com.github.eth0net.enchantmenu.client.gui.screen

import com.github.eth0net.enchantmenu.screen.EnchantMenuScreenHandler
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.Matrix4f
import net.minecraft.util.math.Vec3f

@Environment(EnvType.CLIENT)
class EnchantMenuScreen(handler: EnchantMenuScreenHandler, playerInventory: PlayerInventory, title: Text) :
    HandledScreen<EnchantMenuScreenHandler>(handler, playerInventory, title) {

    private val texture = Identifier("textures/gui/container/enchanting_table.png")
    private var stack = ItemStack.EMPTY
    private var ticks = 0

    override fun handledScreenTick() {
        super.handledScreenTick()
        val itemStack = handler.slots[0].stack
        if (itemStack != stack) stack = itemStack
        ++ticks
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val x = (width - backgroundWidth) / 2
        val y = (height - backgroundHeight) / 2
        val clickX = mouseX - (x + 60).toDouble()

        for (i in handler.enchantments.indices) {
            val clickY = mouseY - (y + 14 + 19 * i).toDouble()
            val inBounds = clickX > 0 && clickX < 108 && clickY > 0 && clickY < 19
            val clicked = handler.onButtonClick(client!!.player as PlayerEntity, i)
            if (!inBounds || !clicked) continue
            client!!.interactionManager!!.clickButton(handler.syncId, i)
            return true
        }

        return super.mouseClicked(mouseX, mouseY, button)
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

        val immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().buffer)
        immediate.draw()
        matrices.pop()
        RenderSystem.viewport(0, 0, client!!.window.framebufferWidth, client!!.window.framebufferHeight)
        RenderSystem.restoreProjectionMatrix()
        DiffuseLighting.enableGuiDepthLighting()
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)

        handler.enchantments.forEachIndexed { index, enchantment ->
            val xOffset = x + 60
            val yOffset = y + 14 + 19 * index
            zOffset = 0
            RenderSystem.setShader(GameRenderer::getPositionTexShader)
            RenderSystem.setShaderTexture(0, texture)
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)

            val text = enchantment.getName(handler.level)
            var color = 6839882

            val mouseOffsetX = mouseX - xOffset
            val mouseOffsetY = mouseY - yOffset
            if (mouseOffsetX >= 0 && mouseOffsetY >= 0 && mouseOffsetX < 108 && mouseOffsetY < 19) {
                drawTexture(matrices, xOffset, yOffset, 0, 204, 108, 19)
                color = 0xFFFF80
            } else {
                drawTexture(matrices, xOffset, yOffset, 0, 166, 108, 19)
            }

            drawTexture(matrices, xOffset + 1, yOffset + 1, 16 * index, 223, 16, 16)
            textRenderer.drawTrimmed(text, xOffset + 20, y + 16 + 19 * index, 86, color)
        }
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, client!!.tickDelta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)

        handler.enchantments.run run@{
            this.forEachIndexed loop@{ i, enchantment ->
                if (!isPointWithinBounds(60, 14 + 19 * i, 108, 17, mouseX.toDouble(), mouseY.toDouble())) return@loop

                val nameText = Text.translatable("container.enchant.clue", enchantment.getName(handler.level))
                val list = listOf(nameText.formatted(Formatting.WHITE))

                renderTooltip(matrices, list, mouseX, mouseY)
                return@run
            }
        }
    }
}

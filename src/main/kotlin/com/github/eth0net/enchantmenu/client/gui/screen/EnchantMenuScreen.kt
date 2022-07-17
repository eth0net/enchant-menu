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
        val i = (width - backgroundWidth) / 2
        val j = (height - backgroundHeight) / 2

        for (k in 0..2) {
            val d = mouseX - (i + 60).toDouble()
            val e = mouseY - (j + 14 + 19 * k).toDouble()
            if (d >= 0f && e >= 0f && d < 108f && e < 19f && handler.onButtonClick(
                    client!!.player as PlayerEntity, k
                )
            ) {
                client!!.interactionManager!!.clickButton(handler.syncId, k)
                return true
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        DiffuseLighting.disableGuiDepthLighting()
        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.setShaderTexture(0, texture)
        val i = (width - backgroundWidth) / 2
        val j = (height - backgroundHeight) / 2
        drawTexture(matrices, i, j, 0, 0, backgroundWidth, backgroundHeight)
        val k = client!!.window.scaleFactor.toInt()
        RenderSystem.viewport((width - 320) / 2 * k, (height - 240) / 2 * k, 320 * k, 240 * k)
        val matrix4f = Matrix4f.translate(-0.34f, 0.23f, 0.0f)
        matrix4f.multiply(Matrix4f.viewboxMatrix(90.0, 1.3333334f, 9.0f, 80.0f))
        RenderSystem.backupProjectionMatrix()
        RenderSystem.setProjectionMatrix(matrix4f)
        matrices.push()
        val entry = matrices.peek()
        entry.positionMatrix.loadIdentity()
        entry.normalMatrix.loadIdentity()
        matrices.translate(0.0, 3.299999952316284, 1984.0)
//        val f = 5.0f
        matrices.scale(5.0f, 5.0f, 5.0f)
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0f))
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(20.0f))
//        val g = MathHelper.lerp(delta, this.pageTurningSpeed, this.nextPageTurningSpeed)
//        matrices.translate(
//            ((1.0f - g) * 0.2f).toDouble(),
//            ((1.0f - g) * 0.1f).toDouble(),
//            ((1.0f - g) * 0.25f).toDouble()
//        )
//        val h = -(1.0f - g) * 90.0f - 90.0f
//        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(h))
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180.0f))
//        var l = MathHelper.lerp(delta, this.pageAngle, this.nextPageAngle) + 0.25f
//        var m = MathHelper.lerp(delta, this.pageAngle, this.nextPageAngle) + 0.75f
//        l = (l - MathHelper.fastFloor(l.toDouble()).toFloat()) * 1.6f - 0.3f
//        m = (m - MathHelper.fastFloor(m.toDouble()).toFloat()) * 1.6f - 0.3f
//        if (l < 0.0f) {
//            l = 0.0f
//        }
//
//        if (m < 0.0f) {
//            m = 0.0f
//        }
//
//        if (l > 1.0f) {
//            l = 1.0f
//        }
//
//        if (m > 1.0f) {
//            m = 1.0f
//        }

//        this.BOOK_MODEL.setPageAngles(0.0f, l, m, g)
        val immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().buffer)
//        val vertexConsumer = immediate.getBuffer(this.BOOK_MODEL.getLayer(EnchantmentScreen.BOOK_TEXTURE))
//        this.BOOK_MODEL.render(matrices, vertexConsumer, 15728880, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f)
        immediate.draw()
        matrices.pop()
        RenderSystem.viewport(0, 0, client!!.window.framebufferWidth, client!!.window.framebufferHeight)
        RenderSystem.restoreProjectionMatrix()
        DiffuseLighting.enableGuiDepthLighting()
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
//        EnchantingPhrases.getInstance().setSeed(handler.seed.toLong())
//        val n = handler.lapisCount

        handler.enchantments!!.forEachIndexed { o, enchantment ->
            val p = i + 60
            val q = p + 20
            zOffset = 0
            RenderSystem.setShader(GameRenderer::getPositionTexShader)
            RenderSystem.setShaderTexture(0, texture)
//            val r = (handler as EnchantmentScreenHandler).enchantmentPower[o]
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
//            val string = "" + r
//            val s = 86 - textRenderer.getWidth(string)
            val s = textRenderer.getWidth(enchantment.toString())
//            val stringVisitable = EnchantingPhrases.getInstance().generatePhrase(textRenderer, s)
            var t = 6839882

            val xOffset = i + 60
            val yOffset = j + 14 + 19 * o

            val u = mouseX - xOffset
            val v = mouseY - yOffset
            if (u >= 0 && v >= 0 && u < 108 && v < 19) {
                drawTexture(matrices, p, yOffset, 0, 204, 108, 19)
                t = 16777088
            } else {
                drawTexture(matrices, p, yOffset, 0, 166, 108, 19)
            }

            drawTexture(matrices, p + 1, yOffset + 1, 16 * o, 223, 16, 16)
            textRenderer.drawTrimmed(Text.literal(enchantment.toString()), q, j + 16 + 19 * o, s, t)
            t = 8453920

            textRenderer.drawWithShadow(
                matrices,
                "string",
                (q + 86 - textRenderer.getWidth("string")).toFloat(),
                (j + 16 + 19 * o + 7).toFloat(),
                t
            )
        }
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        val delta = client!!.tickDelta
        renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)

        handler.enchantments!!.forEachIndexed { index, enchantment ->
            val list: MutableList<Text> = mutableListOf()
            val m = index + 1

            list.add(
                Text.translatable("container.enchant.clue", arrayOf(enchantment.getName(handler.level)))
                    .formatted(Formatting.WHITE)
            )

            val level = if (m == 1) {
                Text.translatable("container.enchant.level.one")
            } else {
                Text.translatable("container.enchant.level.many", arrayOf(m))
            }
            list.add(level.formatted(Formatting.GRAY))

            renderTooltip(matrices, list, mouseX, mouseY)
        }
    }
}

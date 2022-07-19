package com.github.eth0net.enchantmenu

import com.github.eth0net.enchantmenu.screen.EnchantMenuScreenHandler
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Suppress("UNUSED")
object EnchantMenu : ModInitializer {
    private const val MOD_ID = "enchant-menu"
    internal val LOGGER: Logger = LogManager.getLogger(MOD_ID)
    internal val SCREEN_HANDLER =
        Registry.register(Registry.SCREEN_HANDLER, id("enchant_menu"), ScreenHandlerType(::EnchantMenuScreenHandler))
    internal val OPEN_PACKET = id("open_enchant_menu")
    internal val INC_PACKET = id("increment_level")
    internal val DEC_PACKET = id("decrement_level")

    override fun onInitialize() {
        LOGGER.info("EnchantMenu initializing...")

        ServerPlayNetworking.registerGlobalReceiver(OPEN_PACKET) { _, player, _, _, _ ->
            player.openHandledScreen(ScreenHandlerFactory)
        }
        ServerPlayNetworking.registerGlobalReceiver(INC_PACKET) { _, player, _, _, _ ->
            (player.currentScreenHandler as? EnchantMenuScreenHandler)?.incrementLevel()
        }
        ServerPlayNetworking.registerGlobalReceiver(DEC_PACKET) { _, player, _, _, _ ->
            (player.currentScreenHandler as? EnchantMenuScreenHandler)?.decrementLevel()
        }

        LOGGER.info("EnchantMenu initialized.")
    }

    private fun id(path: String) = Identifier(MOD_ID, path)

    object ScreenHandlerFactory : NamedScreenHandlerFactory {
        override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): EnchantMenuScreenHandler {
            return EnchantMenuScreenHandler(syncId, inv)
        }

        override fun getDisplayName(): MutableText = Text.translatable("enchant-menu.title")
    }
}

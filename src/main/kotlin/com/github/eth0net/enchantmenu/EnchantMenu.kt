package com.github.eth0net.enchantmenu

import com.github.eth0net.enchantmenu.screen.EnchantMenuScreenHandler
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
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

    object ScreenHandlerFactory : NamedScreenHandlerFactory {
        override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): EnchantMenuScreenHandler {
            return EnchantMenuScreenHandler(syncId, inv)
        }

        override fun getDisplayName(): MutableText = Text.translatable("Enchant Menu")
    }


    override fun onInitialize() {
        LOGGER.info("EnchantMenu initializing...")

        ServerPlayNetworking.registerGlobalReceiver(OPEN_PACKET) { server, player, handler, buf, res ->
            player.openHandledScreen(ScreenHandlerFactory)
        }

        LOGGER.info("EnchantMenu initialized.")
    }

    private fun id(path: String) = Identifier(MOD_ID, path)
}

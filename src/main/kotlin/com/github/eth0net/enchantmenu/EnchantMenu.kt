package com.github.eth0net.enchantmenu

import com.github.eth0net.enchantmenu.config.EnchantMenuCompleteConfig
import com.github.eth0net.enchantmenu.network.channel.*
import com.github.eth0net.enchantmenu.screen.EnchantMenuScreenHandler
import com.github.eth0net.enchantmenu.screen.EnchantMenuScreenHandlerFactory
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.resource.featuretoggle.FeatureFlags
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import java.io.BufferedWriter
import java.io.StringWriter

@Suppress("UNUSED")
object EnchantMenu : ModInitializer {
    internal const val MOD_ID = "enchant-menu"
    internal val log = LogManager.getLogger(MOD_ID)

    // Registering the screen handler for Enchant Menu
    internal val SCREEN_HANDLER: ScreenHandlerType<EnchantMenuScreenHandler> = Registry.register(
        Registries.SCREEN_HANDLER,
        id("enchant_menu"),
        ScreenHandlerType(
            { syncId, inventory -> EnchantMenuScreenHandler(syncId, inventory) },
            FeatureFlags.DEFAULT_ENABLED_FEATURES
        )
    )

    override fun onInitialize() {
        log.info("EnchantMenu initializing...")

        // Load and apply configuration if 'completeconfig-base' is present
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("completeconfig-base")) {
            try {
                EnchantMenuCompleteConfig.load()
                EnchantMenuCompleteConfig.applyConfig()

                ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
                    val writer = StringWriter()
                    EnchantMenuCompleteConfig.serialize { BufferedWriter(writer) }
                    val buf = PacketByteBufs.create()
                    buf.writeString(writer.toString())
                    ServerPlayNetworking.send(handler.player, ConfigSyncChannel, buf)
                }
            } catch (e: Exception) {
                log.error("Error loading or applying configuration.", e)
            }
        }

        // Registering networking channels for various menu actions
        ServerPlayNetworking.registerGlobalReceiver(MenuChannel) { _, player, _, _, _ ->
            player.openHandledScreen(EnchantMenuScreenHandlerFactory)
        }
        ServerPlayNetworking.registerGlobalReceiver(SearchChannel) { _, player, _, buf, _ ->
            (player.currentScreenHandler as? EnchantMenuScreenHandler)?.search = buf.readString()
        }
        ServerPlayNetworking.registerGlobalReceiver(IncrementLevelChannel) { _, player, _, _, _ ->
            (player.currentScreenHandler as? EnchantMenuScreenHandler)?.incrementLevel()
        }
        ServerPlayNetworking.registerGlobalReceiver(DecrementLevelChannel) { _, player, _, _, _ ->
            (player.currentScreenHandler as? EnchantMenuScreenHandler)?.decrementLevel()
        }
        ServerPlayNetworking.registerGlobalReceiver(ToggleIncompatibleChannel) { _, player, _, _, _ ->
            (player.currentScreenHandler as? EnchantMenuScreenHandler)?.toggleIncompatible()
        }
        ServerPlayNetworking.registerGlobalReceiver(ToggleLevelChannel) { _, player, _, _, _ ->
            (player.currentScreenHandler as? EnchantMenuScreenHandler)?.toggleLevel()
        }
        ServerPlayNetworking.registerGlobalReceiver(ToggleTreasureChannel) { _, player, _, _, _ ->
            (player.currentScreenHandler as? EnchantMenuScreenHandler)?.toggleTreasure()
        }

        log.info("EnchantMenu initialized.")
    }

    internal fun id(path: String): Identifier = Identifier(MOD_ID, path)
}

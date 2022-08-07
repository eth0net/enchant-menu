package com.github.eth0net.enchantmenu.client

import com.github.eth0net.enchantmenu.EnchantMenu
import com.github.eth0net.enchantmenu.client.gui.screen.EnchantMenuScreen
import com.github.eth0net.enchantmenu.client.input.KeyBindings
import com.github.eth0net.enchantmenu.config.EnchantMenuCompleteConfig
import com.github.eth0net.enchantmenu.network.channel.ConfigSyncChannel
import com.github.eth0net.enchantmenu.network.channel.MenuChannel
import me.lortseam.completeconfig.gui.ConfigScreenBuilder
import me.lortseam.completeconfig.gui.cloth.ClothConfigScreenBuilder
import me.shedaniel.clothconfig2.api.ConfigBuilder
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreens
import java.io.BufferedReader
import java.io.StringReader

@Suppress("UNUSED")
object EnchantMenuClient : ClientModInitializer {
    override fun onInitializeClient() {
        EnchantMenu.log.info("EnchantMenuClient initializing...")

        if (FabricLoader.getInstance().isModLoaded("completeconfig")) {
            if (FabricLoader.getInstance().isModLoaded("cloth-config")) {
                ConfigScreenBuilder.setMain(EnchantMenu.MOD_ID, ClothConfigScreenBuilder {
                    val editable = MinecraftClient.getInstance().currentServerEntry == null
                    ConfigBuilder.create().setEditable(editable)
                })
            }

            ClientPlayNetworking.registerGlobalReceiver(ConfigSyncChannel) { _, _, buf, _ ->
                EnchantMenuCompleteConfig.deserialize { BufferedReader(StringReader(buf.readString())) }
            }
        }

        KeyBindings.register()
        HandledScreens.register(EnchantMenu.SCREEN_HANDLER, ::EnchantMenuScreen)

        ClientTickEvents.END_CLIENT_TICK.register {
            while (KeyBindings.ToggleMenu.wasPressed()) ClientPlayNetworking.send(MenuChannel, PacketByteBufs.empty())
        }

        EnchantMenu.log.info("EnchantMenuClient initialized.")
    }
}

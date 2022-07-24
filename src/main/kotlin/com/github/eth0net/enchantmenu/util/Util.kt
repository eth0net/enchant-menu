package com.github.eth0net.enchantmenu.util

import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager

internal const val MOD_ID = "enchant-menu"

internal val Logger = LogManager.getLogger(MOD_ID)

internal fun Identifier(path: String) = Identifier(MOD_ID, path)

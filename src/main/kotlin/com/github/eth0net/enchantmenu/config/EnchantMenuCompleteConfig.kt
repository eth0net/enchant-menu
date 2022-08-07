package com.github.eth0net.enchantmenu.config

import com.github.eth0net.enchantmenu.EnchantMenu
import me.lortseam.completeconfig.api.ConfigContainer
import me.lortseam.completeconfig.api.ConfigContainer.Transitive
import me.lortseam.completeconfig.api.ConfigEntries
import me.lortseam.completeconfig.api.ConfigEntry
import me.lortseam.completeconfig.api.ConfigGroup
import me.lortseam.completeconfig.data.Config

object EnchantMenuCompleteConfig : Config(EnchantMenu.MOD_ID), ConfigContainer {
    @ConfigEntry
    var checkPermission = true

    @Transitive
    @ConfigEntries
    object Levels : ConfigGroup {
        @ConfigEntry.BoundedInteger(min = 1, max = 100)
        @ConfigEntry.Slider
        var minimum = 1
            set(value) {
                field = if (value < 1) {
                    1
                } else if (value > maximum) {
                    maximum
                } else {
                    value
                }
            }

        @ConfigEntry.BoundedInteger(min = 1, max = 100)
        @ConfigEntry.Slider
        var maximum = 10
            set(value) {
                field = if (value < minimum) {
                    minimum
                } else if (value > 100) {
                    100
                } else {
                    value
                }
            }

        @ConfigEntry.BoundedInteger(min = 1, max = 100)
        @ConfigEntry.Slider
        var default = minimum
            set(value) {
                field = if (value < minimum) {
                    minimum
                } else if (value > maximum) {
                    maximum
                } else {
                    value
                }
            }
    }

    @Transitive
    @ConfigEntries
    object AllowLimitBreaks : ConfigGroup {
        var incompatible = true
        var level = true
        var treasure = true
    }

    @Transitive
    @ConfigEntries
    object DefaultLimitBreaks : ConfigGroup {
        var incompatible = false
        var level = false
        var treasure = false
    }

    fun applyConfig() {
        EnchantMenuConfig.checkPermission = checkPermission
        EnchantMenuConfig.Levels.minimum = Levels.minimum
        EnchantMenuConfig.Levels.maximum = Levels.maximum
        EnchantMenuConfig.Levels.default = Levels.default
        EnchantMenuConfig.AllowLimitBreaks.incompatible = AllowLimitBreaks.incompatible
        EnchantMenuConfig.AllowLimitBreaks.level = AllowLimitBreaks.level
        EnchantMenuConfig.AllowLimitBreaks.treasure = AllowLimitBreaks.treasure
        EnchantMenuConfig.DefaultLimitBreaks.incompatible = DefaultLimitBreaks.incompatible
        EnchantMenuConfig.DefaultLimitBreaks.level = DefaultLimitBreaks.level
        EnchantMenuConfig.DefaultLimitBreaks.treasure = DefaultLimitBreaks.treasure
    }

    override fun onUpdate() = applyConfig()
}

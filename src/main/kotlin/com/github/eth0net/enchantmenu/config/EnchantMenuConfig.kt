package com.github.eth0net.enchantmenu.config

import com.github.eth0net.enchantmenu.EnchantMenu
import me.lortseam.completeconfig.api.ConfigContainer
import me.lortseam.completeconfig.api.ConfigContainer.Transitive
import me.lortseam.completeconfig.api.ConfigEntries
import me.lortseam.completeconfig.api.ConfigEntry
import me.lortseam.completeconfig.api.ConfigEntry.BoundedInteger
import me.lortseam.completeconfig.api.ConfigEntry.Slider
import me.lortseam.completeconfig.api.ConfigGroup
import me.lortseam.completeconfig.data.Config

object EnchantMenuConfig : Config(EnchantMenu.MOD_ID), ConfigContainer {
    @ConfigEntry
    var checkPermission = true

    @Transitive
    @ConfigEntries(includeAll = true)
    object Levels : ConfigGroup {
        @BoundedInteger(min = 1, max = 100)
        @Slider
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

        @BoundedInteger(min = 1, max = 100)
        @Slider
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

        @BoundedInteger(min = 1, max = 100)
        @Slider
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
    object AllowLimitBreaks : ConfigGroup {
        @ConfigEntry
        var incompatible = true
        fun toggleIncompatible() {
            incompatible = !incompatible
        }

        @ConfigEntry
        var level = true
        fun toggleLevel() {
            level = !level
        }

        @ConfigEntry
        var treasure = true
        fun toggleTreasure() {
            treasure = !treasure
        }
    }

    @Transitive
    object AutoLimitBreaks : ConfigGroup {
        @ConfigEntry
        var incompatible = false
        fun toggleIncompatible() {
            incompatible = !incompatible
        }

        @ConfigEntry
        var level = false
        fun toggleLevel() {
            level = !level
        }

        @ConfigEntry
        var treasure = false
        fun toggleTreasure() {
            treasure = !treasure
        }
    }
}

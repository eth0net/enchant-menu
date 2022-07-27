package com.github.eth0net.enchantmenu.config

internal class EnchantMenuConfig {
    var minLevel = 1
    var maxLevel = 10
    var defaultLevel = minLevel
        set(value) {
            field = if (value < minLevel) {
                minLevel
            } else if (value > maxLevel) {
                maxLevel
            } else {
                value
            }
        }


    var disableIncompatibleUnlock = false
    fun toggleDisableIncompatibleUnlock() {
        disableIncompatibleUnlock = !disableIncompatibleUnlock
    }

    var disableLevelUnlock = false
    fun toggleDisableLevelUnlock() {
        disableLevelUnlock = !disableLevelUnlock
    }

    var disableTreasureUnlock = false
    fun toggleDisableTreasureUnlock() {
        disableTreasureUnlock = !disableTreasureUnlock
    }


    var defaultIncompatibleUnlocked = false
    fun toggleDefaultIncompatibleUnlocked() {
        defaultIncompatibleUnlocked = !defaultIncompatibleUnlocked
    }

    var defaultLevelUnlocked = false
    fun toggleDefaultLevelUnlocked() {
        defaultLevelUnlocked = !defaultLevelUnlocked
    }

    var defaultTreasureUnlocked = false
    fun toggleDefaultTreasureUnlocked() {
        defaultTreasureUnlocked = !defaultTreasureUnlocked
    }
}

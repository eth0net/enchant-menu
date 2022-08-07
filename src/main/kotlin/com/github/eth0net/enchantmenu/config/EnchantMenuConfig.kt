package com.github.eth0net.enchantmenu.config

object EnchantMenuConfig {
    var checkPermission = true

    object Levels {
        var minimum = 1
        var maximum = 10
        var default = minimum
    }

    object AllowLimitBreaks {
        var incompatible = true
        var level = true
        var treasure = true
    }

    object DefaultLimitBreaks {
        var incompatible = false
        var level = false
        var treasure = false
    }
}

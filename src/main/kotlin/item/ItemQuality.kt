package com.shadowforgedmmo.engine.item

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

enum class ItemQuality(val text: String, val color: TextColor) {
    COMMON("Common", NamedTextColor.WHITE),
    UNCOMMON("Uncommon", NamedTextColor.GREEN),
    RARE("Rare", NamedTextColor.BLUE),
    EPIC("Epic", NamedTextColor.DARK_PURPLE),
    LEGENDARY("Legendary", NamedTextColor.GOLD)
}

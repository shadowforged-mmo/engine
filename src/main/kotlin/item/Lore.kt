package com.shadowforgedmmo.engine.item

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

internal fun Component.noItalic(): Component = decoration(TextDecoration.ITALIC, false)

internal fun loreLine(text: String, color: NamedTextColor): Component =
    Component.text(text, color).noItalic()

internal fun levelLine(level: Int): Component =
    loreLine("Level $level", NamedTextColor.YELLOW)

internal fun requiredLevelLine(level: Int?): Component? =
    level?.let { loreLine("Requires Level $it", NamedTextColor.RED) }

internal fun flavorLine(text: String?): Component? =
    text?.let { loreLine("\"$it\"", NamedTextColor.YELLOW) }

internal fun sellPriceLine(price: Int?): Component? =
    price?.let { loreLine("Sell Price: $it", NamedTextColor.GOLD) }

internal fun useLine(use: String): Component =
    loreLine("Use: $use", NamedTextColor.GREEN)

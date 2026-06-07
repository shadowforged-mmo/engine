package com.shadowforgedmmo.engine.item

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

private val ELEMENTS = listOf("Physical", "Arcane", "Fire", "Frost", "Nature", "Shadow", "Holy")

internal fun elementalLines(values: List<Int>, suffix: String): List<Component> {
    return ELEMENTS.zip(values).mapNotNull { (element, value) ->
        value.takeIf { it > 0 }?.let { loreLine("$it $element $suffix", NamedTextColor.WHITE) }
    }
}

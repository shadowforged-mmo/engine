package com.shadowforgedmmo.engine.zone

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.resource.EnumDeserializer
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

@JsonDeserialize(using = EnumDeserializer::class)
enum class ZoneType(val priority: Int, val color: TextColor) {
    REGION(0, NamedTextColor.YELLOW),
    WILDERNESS(1, NamedTextColor.YELLOW),
    SETTLEMENT(2, NamedTextColor.GREEN),
    DUNGEON(3, NamedTextColor.GRAY)
}

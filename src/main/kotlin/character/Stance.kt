package com.shadowforgedmmo.engine.character

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.resource.deserializeEnum
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

class Stances(
    private val default: Stance,
    private val friendlyIds: Collection<String>,
    private val neutralIds: Collection<String>,
    private val hostileIds: Collection<String>
) {
    fun stance(toward: Character): Stance {
        val id = if (toward is NonPlayerCharacter) "characters:${toward.blueprint.id}" else "player"
        return when (id) {
            in friendlyIds -> Stance.FRIENDLY
            in neutralIds -> Stance.NEUTRAL
            in hostileIds -> Stance.HOSTILE
            else -> {
                val rootSummoner = toward.rootSummoner
                if (rootSummoner is PlayerCharacter) stance(rootSummoner) else default
            }
        }
    }
}

enum class Stance(val color: TextColor) {
    FRIENDLY(NamedTextColor.GREEN),
    NEUTRAL(NamedTextColor.YELLOW),
    HOSTILE(NamedTextColor.RED)
}

fun deserializeStances(data: JsonNode) = Stances(
    data["default"]?.let { deserializeEnum<Stance>(it) } ?: Stance.FRIENDLY,
    data["friendly"]?.map(JsonNode::asText) ?: emptyList(),
    data["neutral"]?.map(JsonNode::asText) ?: emptyList(),
    data["hostile"]?.map(JsonNode::asText) ?: emptyList()
)

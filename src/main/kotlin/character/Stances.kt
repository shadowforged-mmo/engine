package com.shadowforgedmmo.engine.character

import com.fasterxml.jackson.annotation.JsonProperty
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

class Stances(
    private val default: Stance = Stance.FRIENDLY,
    private val friendlyIds: Collection<String> = emptyList(),
    private val neutralIds: Collection<String> = emptyList(),
    private val hostileIds: Collection<String> = emptyList()
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

data class StancesDefinition(
    @JsonProperty("default") val default: Stance?,
    @JsonProperty("friendly") val friendlyReferences: List<String>?,
    @JsonProperty("neutral") val neutralReferences: List<String>?,
    @JsonProperty("hostile") val hostileReferences: List<String>?
) {
    fun toStances() = Stances(
        default ?: Stance.FRIENDLY,
        friendlyReferences ?: emptyList(),
        neutralReferences ?: emptyList(),
        hostileReferences ?: emptyList()
    )
}

enum class Stance(val color: TextColor) {
    FRIENDLY(NamedTextColor.GREEN),
    NEUTRAL(NamedTextColor.YELLOW),
    HOSTILE(NamedTextColor.RED)
}

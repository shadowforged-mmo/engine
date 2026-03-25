package com.shadowforgedmmo.engine.character

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.resource.EnumDeserializer
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

data class StancesDefinition(
    @JsonProperty("default") val default: Stance,
    @JsonProperty("friendly") val friendlyReferences: List<CharacterBlueprintReference>,
    @JsonProperty("neutral") val neutralReferences: List<CharacterBlueprintReference>,
    @JsonProperty("hostile") val hostileReferences: List<CharacterBlueprintReference>
) {
    fun toStances() = Stances(
        default,
        friendlyReferences.map(CharacterBlueprintReference::id),
        neutralReferences.map(CharacterBlueprintReference::id),
        hostileReferences.map(CharacterBlueprintReference::id)
    )
}

@JsonDeserialize(using = EnumDeserializer::class)
enum class Stance(val color: TextColor) {
    FRIENDLY(NamedTextColor.GREEN),
    NEUTRAL(NamedTextColor.YELLOW),
    HOSTILE(NamedTextColor.RED)
}

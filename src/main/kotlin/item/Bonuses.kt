package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

data class Bonuses(
    val strength: Int = 0,
    val dexterity: Int = 0,
    val intelligence: Int = 0,
    val vitality: Int = 0,
    val health: Int = 0,
    val mana: Int = 0,
) {
    fun components(): List<Component> {
        fun line(amount: Int, label: String) =
            amount.takeIf { it > 0 }?.let { loreLine("+$it $label", NamedTextColor.BLUE) }
        return listOfNotNull(
            line(strength, "Strength"),
            line(dexterity, "Dexterity"),
            line(intelligence, "Intelligence"),
            line(vitality, "Vitality"),
            line(health, "Health"),
            line(mana, "Mana"),
        )
    }
}

data class BonusesDefinition(
    @JsonProperty("strength") val strength: Int? = null,
    @JsonProperty("dexterity") val dexterity: Int? = null,
    @JsonProperty("intelligence") val intelligence: Int? = null,
    @JsonProperty("vitality") val vitality: Int? = null,
    @JsonProperty("health") val health: Int? = null,
    @JsonProperty("mana") val mana: Int? = null,
) {
    fun toBonuses() = Bonuses(
        strength ?: 0,
        dexterity ?: 0,
        intelligence ?: 0,
        vitality ?: 0,
        health ?: 0,
        mana ?: 0,
    )
}

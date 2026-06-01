package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty

data class WeaponDamage(
    @JsonProperty("physical") val physical: Int = 0,
    @JsonProperty("arcane") val arcane: Int = 0,
    @JsonProperty("fire") val fire: Int = 0,
    @JsonProperty("frost") val frost: Int = 0,
    @JsonProperty("nature") val nature: Int = 0,
    @JsonProperty("shadow") val shadow: Int = 0,
    @JsonProperty("holy") val holy: Int = 0,
) {
    fun components() = elementalLines(
        listOf(physical, arcane, fire, frost, nature, shadow, holy), "Damage"
    )
}

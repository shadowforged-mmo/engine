package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty

data class Bonuses(
    @JsonProperty("strength") val strength: Int? = null,
    @JsonProperty("dexterity") val dexterity: Int? = null,
    @JsonProperty("intelligence") val intelligence: Int? = null,
    @JsonProperty("vitality") val vitality: Int? = null,
    @JsonProperty("health") val health: Int? = null,
    @JsonProperty("mana") val mana: Int? = null
)

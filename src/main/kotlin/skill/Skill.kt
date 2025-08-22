package com.shadowforgedmmo.engine.skill

import com.fasterxml.jackson.databind.JsonNode

abstract class Skill(
    val id: String,
    val name: String,
    val description: String,
    val scriptId: String
)

fun deserializeSkill(
    id: String,
    data: JsonNode
) = when (data["type"].asText()) {
    "active" -> deserializeActiveSkill(id, data)
    "passive" -> deserializePassiveSkill(id, data)
    else -> throw IllegalArgumentException()
}

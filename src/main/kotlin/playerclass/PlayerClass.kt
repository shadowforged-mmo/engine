package com.shadowforgedmmo.engine.playerclass

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.skill.Skill

class PlayerClass(val id: String, val skills: List<Skill>)

fun deserializePlayerClass(id: String, data: JsonNode, skillsById: Map<String, Skill>) = PlayerClass(
    id,
    data["skills"].map(JsonNode::asText).map(skillsById::getValue)
)

package com.shadowforgedmmo.engine.playerclass

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.resource.parseId
import com.shadowforgedmmo.engine.skill.Skill
import com.shadowforgedmmo.engine.skill.parseSkillId

class PlayerClass(val id: String, val skills: List<Skill>)

fun deserializePlayerClass(id: String, data: JsonNode, skillsById: Map<String, Skill>) = PlayerClass(
    id,
    data["skills"].map(JsonNode::asText).map(::parseSkillId).map(skillsById::getValue)
)

fun parsePlayerClassId(id: String) = parseId(id, "classes")

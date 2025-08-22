package com.shadowforgedmmo.engine.skill

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.script.parseScriptId

class PassiveSkill(
    id: String,
    name: String,
    description: String,
    scriptId: String
) : Skill(id, name, description, scriptId)

fun deserializePassiveSkill(
    id: String,
    data: JsonNode
) = PassiveSkill(
    id,
    data["name"].asText(),
    data["description"].asText(),
    parseScriptId(data["scriptId"].asText())
)

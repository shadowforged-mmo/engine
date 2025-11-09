package com.shadowforgedmmo.engine.persistence

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.quest.Quest
import com.shadowforgedmmo.engine.quest.parseQuestId
import com.shadowforgedmmo.engine.runtime.Runtime

class QuestData(
    val completed: Set<Quest>,
    val tracked: Set<Quest>,
    val objectives: Map<String, IntArray>
)

fun deserializeQuestData(data: JsonNode, runtime: Runtime) = QuestData(
    data["completed"]
        .map(JsonNode::asText)
        .map(::parseQuestId)
        .map(runtime.questsById::getValue)
        .toSet(),
    data["tracked"]
        .map(JsonNode::asText)
        .map(::parseQuestId)
        .map(runtime.questsById::getValue)
        .toSet(),
    data["objectives"].associate {
        parseQuestId(it["quest"].asText()) to it["progress"].map(JsonNode::asInt).toIntArray()
    }
)

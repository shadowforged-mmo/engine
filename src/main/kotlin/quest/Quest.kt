package com.shadowforgedmmo.engine.quest

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.resource.QUESTS
import com.shadowforgedmmo.engine.resource.ResourceReference
import com.shadowforgedmmo.engine.resource.ResourceReferenceDeserializer
import com.shadowforgedmmo.engine.runtime.Runtime

class Quest(
    val id: String,
    val name: String,
    val level: Int,
    val prerequisiteReferences: Collection<QuestReference>,
    val objectives: List<QuestObjective>
) {
    fun start(runtime: Runtime) {
        for ((index, objective) in objectives.withIndex()) {
            objective.start(runtime, this, index)
        }
    }
}

data class QuestDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("level") val level: Int,
    @JsonProperty("prerequisites") val prerequisiteReferences: List<QuestReference>?,
    @JsonProperty("objectives") val objectives: List<QuestObjectiveDefinition>?,
    @JsonProperty("rewards") val rewards: QuestRewardsDefinition?
) {
    fun toQuest(id: String) = Quest(
        id,
        name,
        level,
        prerequisiteReferences ?: emptyList(),
        objectives?.map(QuestObjectiveDefinition::toQuestObjective) ?: emptyList()
    )
}

@JsonDeserialize(using = QuestReferenceDeserializer::class)
class QuestReference(id: String) : ResourceReference(id)

class QuestReferenceDeserializer : ResourceReferenceDeserializer<QuestReference>(
    QUESTS,
    ::QuestReference
)

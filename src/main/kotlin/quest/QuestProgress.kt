package com.shadowforgedmmo.engine.quest

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.resource.Registry

class QuestProgress(
    val completedQuestIds: Set<String>,
    val tracked: Set<Quest>,
    val objectivesByQuestId: Map<String, IntArray>
)

data class QuestProgressDefinition(
    @JsonProperty("completed") val completedQuestReferences: List<QuestReference>,
    @JsonProperty("tracked") val trackedQuestReferences: List<QuestReference>,
    @JsonProperty("objectives") val objectives: List<QuestObjectiveProgressDefinition>
) {
    fun toQuestProgress(questRegistry: Registry<Quest>) = QuestProgress(
        completedQuestReferences.map(QuestReference::id).toSet(),
        trackedQuestReferences.map { it.resolve(questRegistry) }.toSet(),
        objectives.associate { (questReference, progress) ->
            Pair(questReference.id, progress.toIntArray())
        }
    )
}

data class QuestObjectiveProgressDefinition(
    @JsonProperty("quest") val questReference: QuestReference,
    @JsonProperty("progress") val progress: List<Int>
)

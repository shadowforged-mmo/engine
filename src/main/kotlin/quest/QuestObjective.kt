package com.shadowforgedmmo.engine.quest

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.shadowforgedmmo.engine.character.CharacterBlueprint
import com.shadowforgedmmo.engine.character.CharacterBlueprintReference
import com.shadowforgedmmo.engine.item.Item
import com.shadowforgedmmo.engine.item.ItemReference
import com.shadowforgedmmo.engine.item.QuestItem
import com.shadowforgedmmo.engine.runtime.Runtime

abstract class QuestObjective(val goal: Int, val markers: Collection<QuestObjectiveMarker>) {
    abstract val description: String

    open fun start(runtime: Runtime, quest: Quest, objectiveIndex: Int) = Unit
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SlayCharacterObjectiveDefinition::class, name = "slay_character"),
    JsonSubTypes.Type(value = CollectItemObjectiveDefinition::class, name = "collect_item")
)
sealed class QuestObjectiveDefinition {
    abstract fun toQuestObjective(): QuestObjective
}

class SlayCharacterObjective(
    goal: Int,
    markers: Collection<QuestObjectiveMarker>,
    val characterBlueprintReference: CharacterBlueprintReference
) : QuestObjective(goal, markers) {
    override val description
        get() = "Slay ${characterBlueprint?.name}"

    private lateinit var characterBlueprint: CharacterBlueprint

    override fun start(runtime: Runtime, quest: Quest, objectiveIndex: Int) {
        characterBlueprint = characterBlueprintReference.resolve(runtime.resources.characterBlueprintRegistry)
        runtime.questObjectiveManager.registerSlayObjective(
            quest,
            objectiveIndex,
            characterBlueprintReference.id
        )
    }
}

data class SlayCharacterObjectiveDefinition(
    @JsonProperty("goal") val goal: Int,
    @JsonProperty("markers") val markers: Collection<QuestObjectiveMarker>,
    @JsonProperty("character") val characterBlueprintReference: CharacterBlueprintReference
) : QuestObjectiveDefinition() {
    override fun toQuestObjective() = SlayCharacterObjective(
        goal,
        markers,
        characterBlueprintReference
    )
}

class CollectItemObjective(
    goal: Int,
    markers: List<QuestObjectiveMarker>,
    val itemReference: ItemReference
) : QuestObjective(goal, markers) {
    override val description: String
        get() = "${item?.name}"

    private lateinit var item: Item

    override fun start(runtime: Runtime, quest: Quest, objectiveIndex: Int) {
        item = itemReference.resolve(runtime.resources.itemRegistry)
        if (item !is QuestItem) error("Item is not a quest item: $item.id")
        runtime.questObjectiveManager.registerItemCollectObjective(
            quest,
            objectiveIndex,
            itemReference.id
        )
    }
}

data class CollectItemObjectiveDefinition(
    @JsonProperty("goal") val goal: Int,
    @JsonProperty("markers") val markers: List<QuestObjectiveMarker>,
    @JsonProperty("item") val itemReference: ItemReference
) : QuestObjectiveDefinition() {
    override fun toQuestObjective() = CollectItemObjective(
        goal,
        markers,
        itemReference
    )
}

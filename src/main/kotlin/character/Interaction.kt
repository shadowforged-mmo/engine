package com.shadowforgedmmo.engine.character

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.shadowforgedmmo.engine.instance.Instance
import com.shadowforgedmmo.engine.math.Position
import com.shadowforgedmmo.engine.quest.Quest
import com.shadowforgedmmo.engine.quest.QuestReference
import com.shadowforgedmmo.engine.resource.Registry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

abstract class Interaction {
    abstract fun isAvailable(pc: PlayerCharacter): Boolean

    abstract fun start(instance: Instance, position: Position)

    abstract fun advance(
        npc: NonPlayerCharacter,
        pc: PlayerCharacter,
        index: Int
    ): Boolean
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = StartQuestInteractionDefinition::class, name = "start_quest"),
    JsonSubTypes.Type(value = TurnInQuestInteractionDefinition::class, name = "turn_in_quest")
)
sealed class InteractionDefinition {
    abstract fun toInteraction(questRegistry: Registry<Quest>): Interaction
}

class StartQuestInteraction(
    private val quest: Quest,
    private val dialogue: List<Component>
) : Interaction() {
    override fun isAvailable(pc: PlayerCharacter) =
        pc.questTracker.isReadyToStart(quest)

    override fun start(instance: Instance, position: Position) {
        instance.questStarts.put(position.toVector2(), quest)
    }

    override fun advance(
        npc: NonPlayerCharacter,
        pc: PlayerCharacter,
        index: Int
    ): Boolean {
        if (index < dialogue.size) {
            npc.speak(dialogue[index], pc)
            return true
        } else {
            pc.questTracker.startQuest(quest)
            return false
        }
    }
}

data class StartQuestInteractionDefinition(
    @JsonProperty("quest") private val questReference: QuestReference,
    @JsonProperty("dialogue") private val dialogue: List<String>
) : InteractionDefinition() {
    override fun toInteraction(questRegistry: Registry<Quest>) = StartQuestInteraction(
        questReference.resolve(questRegistry),
        dialogue.map(MiniMessage.miniMessage()::deserialize)
    )
}

class TurnInQuestInteraction(
    private val quest: Quest,
    private val dialogue: List<Component>
) : Interaction() {
    override fun isAvailable(pc: PlayerCharacter) =
        pc.questTracker.isReadyToTurnIn(quest)

    override fun start(instance: Instance, position: Position) {
        instance.questTurnIns.put(position.toVector2(), quest)
    }

    override fun advance(
        npc: NonPlayerCharacter,
        pc: PlayerCharacter,
        index: Int
    ): Boolean {
        if (index < dialogue.size) {
            npc.speak(dialogue[index], pc)
            return true
        } else {
            pc.questTracker.completeQuest(quest)
            return false
        }
    }
}

data class TurnInQuestInteractionDefinition(
    @JsonProperty("quest") private val questReference: QuestReference,
    @JsonProperty("dialogue") private val dialogue: List<String>
) : InteractionDefinition() {
    override fun toInteraction(questRegistry: Registry<Quest>) = TurnInQuestInteraction(
        questReference.resolve(questRegistry),
        dialogue.map(MiniMessage.miniMessage()::deserialize)
    )
}

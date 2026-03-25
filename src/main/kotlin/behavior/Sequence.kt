package com.shadowforgedmmo.engine.behavior

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.NonPlayerCharacter

class Sequence(children: List<Behavior>) : Composite(children) {
    private var currentChild = 0

    override fun start(character: NonPlayerCharacter) {
        currentChild = 0
    }

    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        while (currentChild < children.size) {
            val status = children[currentChild].tick(character)
            if (status != BehaviorStatus.SUCCESS) {
                return status
            }
            currentChild++
        }
        return BehaviorStatus.SUCCESS
    }
}

class SequenceBlueprint(
    private val children: List<BehaviorBlueprint>
) : BehaviorBlueprint() {
    override fun create() = Sequence(children.map(BehaviorBlueprint::create))
}

data class SequenceDefinition(
    @JsonProperty("children") val children: List<BehaviorDefinition>
) : BehaviorDefinition() {
    override fun toBlueprint() =
        SequenceBlueprint(children.map(BehaviorDefinition::toBlueprint))
}
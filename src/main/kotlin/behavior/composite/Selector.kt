package com.shadowforgedmmo.engine.behavior.composite

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.behavior.*
import com.shadowforgedmmo.engine.character.NonPlayerCharacter

class Selector(children: List<Behavior>) : Composite(children) {
    private var currentChild = 0

    override fun start(character: NonPlayerCharacter) {
        currentChild = 0
    }

    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        while (currentChild < children.size) {
            val status = children[currentChild].tick(character)
            if (status != BehaviorStatus.FAILURE) {
                return status
            }
            currentChild++
        }
        return BehaviorStatus.FAILURE
    }
}

class SelectorBlueprint(
    private val children: List<BehaviorBlueprint>
) : BehaviorBlueprint() {
    override fun create() = Selector(children.map(BehaviorBlueprint::create))
}

fun deserializeSelectorBlueprint(data: JsonNode) =
    SelectorBlueprint(data["children"].map(::deserializeBehaviorBlueprint))

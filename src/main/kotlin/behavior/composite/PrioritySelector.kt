package com.shadowforgedmmo.engine.behavior.composite

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.behavior.*
import com.shadowforgedmmo.engine.character.NonPlayerCharacter

class PrioritySelector(children: List<Behavior>) : Composite(children) {
    private var currentChild = 0

    override fun start(character: NonPlayerCharacter) {
        currentChild = 0
    }

    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val prevChild = currentChild
        currentChild = 0

        var status = BehaviorStatus.FAILURE

        while (currentChild < children.size) {
            val childStatus = children[currentChild].tick(character)

            if (childStatus != BehaviorStatus.FAILURE) {
                status = childStatus
                break
            }

            currentChild++
        }

        if (prevChild != children.size && currentChild != prevChild) {
            children[prevChild].abort(character)
        }

        return status
    }
}

class PrioritySelectorBlueprint(
    private val children: List<BehaviorBlueprint>
) : BehaviorBlueprint() {
    override fun create() = PrioritySelector(children.map(BehaviorBlueprint::create))
}

fun deserializePrioritySelectorBlueprint(data: JsonNode) =
    PrioritySelectorBlueprint(data["children"].map(::deserializeBehaviorBlueprint))

package com.shadowforgedmmo.engine.behavior

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.NonPlayerCharacter

class LoopForever(child: Behavior) : Decorator(child) {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        while (true) {
            val status = child.tick(character)

            if (status != BehaviorStatus.SUCCESS) {
                return status
            }
        }
    }
}

class LoopForeverBlueprint(
    private val child: BehaviorBlueprint
) : BehaviorBlueprint() {
    override fun create() = LoopForever(child.create())
}

data class LoopForeverDefinition(
    @JsonProperty("child") val child: BehaviorDefinition
) : BehaviorDefinition() {
    override fun toBlueprint() =
        LoopForeverBlueprint(child.toBlueprint())
}

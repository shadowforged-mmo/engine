package com.shadowforgedmmo.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.ai.behavior.Behavior
import com.shadowforgedmmo.engine.ai.behavior.BehaviorBlueprint
import com.shadowforgedmmo.engine.ai.behavior.BehaviorStatus
import com.shadowforgedmmo.engine.character.NonPlayerCharacter

class HasTarget : Behavior() {
    override fun update(character: NonPlayerCharacter) =
        character.target?.let { BehaviorStatus.SUCCESS } ?: BehaviorStatus.FAILURE
}

class HasTargetBlueprint : BehaviorBlueprint() {
    override fun create() = HasTarget()
}

fun deserializeHasTargetBlueprint(
    @Suppress("UNUSED_PARAMETER") data: JsonNode
) = HasTargetBlueprint()

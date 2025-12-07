package com.shadowforgedmmo.engine.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.behavior.Behavior
import com.shadowforgedmmo.engine.behavior.BehaviorBlueprint
import com.shadowforgedmmo.engine.behavior.BehaviorStatus
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

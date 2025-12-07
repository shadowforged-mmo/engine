package com.shadowforgedmmo.engine.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.behavior.BehaviorBlueprint
import com.shadowforgedmmo.engine.behavior.BehaviorStatus
import com.shadowforgedmmo.engine.behavior.Task
import com.shadowforgedmmo.engine.character.NonPlayerCharacter

class FaceTarget : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val target = character.target ?: return BehaviorStatus.FAILURE
        character.lookAt(target)
        return BehaviorStatus.SUCCESS
    }
}

class FaceTargetBlueprint : BehaviorBlueprint() {
    override fun create() = FaceTarget()
}

fun deserializeFaceTargetBlueprint(
    @Suppress("UNUSED_PARAMETER") data: JsonNode
) = FaceTargetBlueprint()

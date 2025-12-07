package com.shadowforgedmmo.engine.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.behavior.BehaviorBlueprint
import com.shadowforgedmmo.engine.behavior.BehaviorStatus
import com.shadowforgedmmo.engine.behavior.Task
import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import com.shadowforgedmmo.engine.math.Vector3
import com.shadowforgedmmo.engine.math.deserializeVector3

class SetVelocity(private val velocity: Vector3) : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        character.velocity = character.position.localToGlobalDirection(velocity)
        return BehaviorStatus.SUCCESS
    }
}

class SetVelocityBlueprint(
    private val velocity: Vector3
) : BehaviorBlueprint() {
    override fun create() = SetVelocity(velocity)
}

fun deserializeSetVelocityBlueprint(data: JsonNode) = SetVelocityBlueprint(
    deserializeVector3(data["velocity"])
)

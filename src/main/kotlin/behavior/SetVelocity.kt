package com.shadowforgedmmo.engine.behavior

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import com.shadowforgedmmo.engine.math.Vector3

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

data class SetVelocityDefinition(
    @JsonProperty("velocity") val velocity: Vector3
) : BehaviorDefinition() {
    override fun toBlueprint() =
        SetVelocityBlueprint(velocity)
}

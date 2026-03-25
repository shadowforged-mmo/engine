package com.shadowforgedmmo.engine.behavior

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.NonPlayerCharacter

class Inverter(child: Behavior) : Decorator(child) {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        return when (child.tick(character)) {
            BehaviorStatus.SUCCESS -> BehaviorStatus.FAILURE
            BehaviorStatus.FAILURE -> BehaviorStatus.SUCCESS
            else -> status
        }
    }
}

class InverterBlueprint(
    private val child: BehaviorBlueprint
) : BehaviorBlueprint() {
    override fun create() = Inverter(child.create())
}

data class InverterDefinition(
    @JsonProperty("child") val child: BehaviorDefinition
) : BehaviorDefinition() {
    override fun toBlueprint() =
        InverterBlueprint(child.toBlueprint())
}

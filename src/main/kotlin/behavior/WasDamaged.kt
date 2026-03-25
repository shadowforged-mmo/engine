package com.shadowforgedmmo.engine.behavior

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import com.shadowforgedmmo.engine.time.secondsToMillis

class WasDamaged(val durationMillis: Long) : Task() {
    override fun update(character: NonPlayerCharacter) =
//        if (timeMillis - character.lastDamagedTimeMillis <= durationMillis) BehaviorStatus.SUCCESS
//        else BehaviorStatus.FAILURE
        BehaviorStatus.FAILURE
}

class WasDamagedBlueprint(
    private val durationMillis: Long
) : BehaviorBlueprint() {
    override fun create() = WasDamaged(durationMillis)
}

data class WasDamagedDefinition(
    @JsonProperty("duration") val duration: Double
) : BehaviorDefinition() {
    override fun toBlueprint() =
        WasDamagedBlueprint(secondsToMillis(duration))
}

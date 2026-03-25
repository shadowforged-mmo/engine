package com.shadowforgedmmo.engine.behavior

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import com.shadowforgedmmo.engine.time.secondsToMillis

class Wait(private val durationMillis: Long) : Task() {
    private var startTimeMillis = 0L

    override fun start(character: NonPlayerCharacter) {
        startTimeMillis = character.runtime.timeMillis
    }

    override fun update(character: NonPlayerCharacter) =
        if (character.runtime.timeMillis - startTimeMillis >= durationMillis) {
            BehaviorStatus.SUCCESS
        } else {
            BehaviorStatus.RUNNING
        }
}

class WaitBlueprint(
    private val durationMillis: Long
) : BehaviorBlueprint() {
    override fun create() = Wait(durationMillis)
}

data class WaitDefinition(
    @JsonProperty("duration") val duration: Double
) : BehaviorDefinition() {
    override fun toBlueprint() =
        WaitBlueprint(secondsToMillis(duration))
}

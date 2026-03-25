package com.shadowforgedmmo.engine.behavior

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import com.shadowforgedmmo.engine.time.Cooldown

class WithCooldown(child: Behavior, cooldownMillis: Long) : Decorator(child) {
    private val cooldown = Cooldown(cooldownMillis)

    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val timeMillis = character.runtime.timeMillis
        if (cooldown.hasCooldown(timeMillis)) {
            return BehaviorStatus.FAILURE
        }
        val status = child.tick(character)
        if (status != BehaviorStatus.RUNNING) {
            cooldown.set(timeMillis)
        }
        return status
    }
}

class WithCooldownBlueprint(
    private val child: BehaviorBlueprint,
    private val cooldownMillis: Long
) : BehaviorBlueprint() {
    override fun create() = WithCooldown(child.create(), cooldownMillis)
}

data class WithCooldownDefinition(
    @JsonProperty("child") val child: BehaviorDefinition,
    @JsonProperty("cooldown") val cooldown: Long
) : BehaviorDefinition() {
    override fun toBlueprint() =
        WithCooldownBlueprint(child.toBlueprint(), cooldown)
}

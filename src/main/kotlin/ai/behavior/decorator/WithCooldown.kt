package com.shadowforgedmmo.engine.ai.behavior.decorator

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.ai.behavior.Behavior
import com.shadowforgedmmo.engine.ai.behavior.BehaviorBlueprint
import com.shadowforgedmmo.engine.ai.behavior.BehaviorStatus
import com.shadowforgedmmo.engine.ai.behavior.Decorator
import com.shadowforgedmmo.engine.ai.behavior.deserializeBehaviorBlueprint
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

fun deserializeWithCooldownBlueprint(data: JsonNode) = WithCooldownBlueprint(
    deserializeBehaviorBlueprint(data["child"]),
    data["cooldown"].asLong()
)

package com.shadowforgedmmo.engine.behavior

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import com.shadowforgedmmo.engine.math.Vector3
import kotlin.random.Random

private const val ACCEPTANCE_RADIUS = 1.0

class GoToRandomPosition(
    private val radius: Double,
    private val movementSpeed: Double
) : Task() {
    override fun start(character: NonPlayerCharacter) {
        val position = character.position.toVector3()
        for (i in 0..<(radius * radius * radius).toInt()) {
            val offset = Vector3(
                Random.nextDouble(-radius, radius),
                Random.nextDouble(-radius, radius),
                Random.nextDouble(-radius, radius)
            )
            val target = position + offset
            if (character.navigator.setPathTo(target, movementSpeed)) break
        }
    }

    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val pathPosition = character.navigator.pathPosition
            ?: return BehaviorStatus.FAILURE
        return if (Vector3.sqrDistance(
                character.position.toVector3(),
                pathPosition
            ) <= ACCEPTANCE_RADIUS * ACCEPTANCE_RADIUS
        )
            BehaviorStatus.SUCCESS
        else
            BehaviorStatus.RUNNING
    }

    override fun stop(character: NonPlayerCharacter) {
        character.navigator.reset()
    }
}

class GoToRandomPositionBlueprint(
    private val radius: Double,
    private val movementSpeed: Double
) : BehaviorBlueprint() {
    override fun create() = GoToRandomPosition(radius, movementSpeed)
}

data class GoToRandomPositionDefinition(
    @JsonProperty("radius") val radius: Double,
    @JsonProperty("speed") val speed: Double
) : BehaviorDefinition() {
    override fun toBlueprint() =
        GoToRandomPositionBlueprint(radius, speed)
}

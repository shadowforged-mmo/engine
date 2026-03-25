package com.shadowforgedmmo.engine.behavior

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import com.shadowforgedmmo.engine.math.Position
import kotlin.math.pow

class IsTargetInRange(
    private val minDistance: Double,
    private val maxDistance: Double
) : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val target = character.target ?: return BehaviorStatus.FAILURE
        val sqrDistanceToTarget = Position.sqrDistance(character.position, target.position)
        return if (sqrDistanceToTarget in minDistance.pow(2)..maxDistance.pow(2))
            BehaviorStatus.SUCCESS
        else
            BehaviorStatus.FAILURE
    }
}

class IsTargetInRangeBlueprint(
    private val minDistance: Double,
    private val maxDistance: Double
) : BehaviorBlueprint() {
    override fun create() = IsTargetInRange(minDistance, maxDistance)
}

data class IsTargetInRangeDefinition(
    @JsonProperty("min_distance") val minDistance: Double = 0.0,
    @JsonProperty("max_distance") val maxDistance: Double = Double.MAX_VALUE
) : BehaviorDefinition() {
    override fun toBlueprint() =
        IsTargetInRangeBlueprint(minDistance, maxDistance)
}

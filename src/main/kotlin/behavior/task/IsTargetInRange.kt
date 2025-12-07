package com.shadowforgedmmo.engine.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.behavior.BehaviorBlueprint
import com.shadowforgedmmo.engine.behavior.BehaviorStatus
import com.shadowforgedmmo.engine.behavior.Task
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

fun deserializeIsTargetInRangeBlueprint(data: JsonNode) = IsTargetInRangeBlueprint(
    data["min_distance"]?.let(JsonNode::asDouble) ?: 0.0,
    data["max_distance"]?.let(JsonNode::asDouble) ?: Double.MAX_VALUE
)

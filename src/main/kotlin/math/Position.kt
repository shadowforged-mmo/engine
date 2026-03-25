package com.shadowforgedmmo.engine.math

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.node.ArrayNode
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.sin

@JsonDeserialize(using = PositionDeserializer::class)
data class Position(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Double = 0.0,
    val pitch: Double = 0.0
) {
    companion object {
        fun distance(a: Position, b: Position) =
            Vector3.distance(a.toVector3(), b.toVector3())

        fun sqrDistance(a: Position, b: Position) =
            Vector3.sqrDistance(a.toVector3(), b.toVector3())
    }

    val direction: Vector3
        get() {
            val cosPitch = cos(toRadians(pitch))
            return Vector3(
                -cosPitch * sin(toRadians(yaw)),
                -sin(toRadians(pitch)),
                cosPitch * cos(toRadians(yaw))
            )
        }

    fun toVector3() = Vector3(x, y, z)

    fun toVector2() = Vector2(x, z)

    operator fun plus(vector3: Vector3) = Position(
        x + vector3.x,
        y + vector3.y,
        z + vector3.z,
        yaw,
        pitch
    )

    operator fun minus(vector3: Vector3) = Position(
        x - vector3.x,
        y - vector3.y,
        z - vector3.z,
        yaw,
        pitch
    )

    fun localToGlobalDirection(localDirection: Vector3) =
        localDirection.rotateAroundY(toRadians(-yaw))

    fun globalToLocalDirection(globalDirection: Vector3) =
        globalDirection.rotateAroundY(toRadians(yaw))
}

class PositionDeserializer : JsonDeserializer<Position>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): Position {
        val node = p.codec.readTree<ArrayNode>(p)
        val x = node[0].asDouble()
        val y = node[1].asDouble()
        val z = node[2].asDouble()
        if (node.size() == 3) return Position(x, y, z)
        val yaw = node[3].asDouble()
        val pitch = node[4].asDouble()
        return Position(x, y, z, yaw, pitch)
    }
}
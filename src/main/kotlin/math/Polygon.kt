package com.shadowforgedmmo.engine.math

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.node.ArrayNode

@JsonDeserialize(using = PolygonDeserializer::class)
data class Polygon(val points: List<Vector2>) {
    init {
        require(points.size >= 3)
    }

    val boundingBox = BoundingBox2(
        points.reduce(Vector2::minOf),
        points.reduce(Vector2::maxOf)
    )

    fun contains(point: Vector2): Boolean {
        if (!boundingBox.contains(point)) return false
        var inside = false
        for (i in points.indices) {
            val j = (i - 1).mod(points.size)
            val edge = points[j] - points[i]
            if (points[i].y > point.y != points[j].y > point.y &&
                point.x < edge.x * (point.y - points[i].y) / edge.y + points[i].x
            ) {
                inside = !inside
            }
        }
        return inside
    }

    fun offset(d: Double): Polygon {
        val edges = points.indices.map {
            points[(it + 1).mod(points.size)] - points[it]
        }

        val normals = edges.map { Vector2(it.y, -it.x).normalized }

        return Polygon(
            points.indices.map {
                val rightPoint = points[(it - 1).mod(points.size)]
                val point = points[it]
                val leftPoint = points[(it + 1).mod(points.size)]
                val rightNormal = normals[(it - 1).mod(points.size)]
                val leftNormal = normals[it]
                lineIntersection(
                    rightPoint + rightNormal * d,
                    point + rightNormal * d,
                    point + leftNormal * d,
                    leftPoint + leftNormal * d
                ) ?: (point + rightNormal * d)
            }
        )
    }
}

class PolygonDeserializer : JsonDeserializer<Polygon>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): Polygon {
        val node = p.codec.readTree<ArrayNode>(p)

        if (node.size() < 3) {
            throw JsonMappingException.from(p, "Polygon must have at least 3 vertices")
        }

        val points = node.map { p.codec.treeToValue(it, Vector2::class.java) }

        return Polygon(points)
    }
}

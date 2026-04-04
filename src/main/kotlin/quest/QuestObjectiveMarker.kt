package com.shadowforgedmmo.engine.quest

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.shadowforgedmmo.engine.instance.InstanceReference
import com.shadowforgedmmo.engine.math.Polygon
import com.shadowforgedmmo.engine.math.Vector2
import java.awt.Graphics

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = PointQuestObjectiveMarker::class, name = "point"),
    JsonSubTypes.Type(value = PolygonQuestObjectiveMarker::class, name = "polygon")
)
abstract class QuestObjectiveMarker {
    abstract fun draw(graphics: Graphics)
}

class PointQuestObjectiveMarker(
    @JsonProperty("instance") private val instanceReference: InstanceReference,
    @JsonProperty("point") private val point: Vector2
) : QuestObjectiveMarker() {
    override fun draw(graphics: Graphics) {
        graphics.fillOval(point.x.toInt(), point.y.toInt(), 10, 10)
    }
}

data class PolygonQuestObjectiveMarker(
    @JsonProperty("instance") private val instanceReference: InstanceReference,
    @JsonProperty("boundary") private val boundary: Polygon
) : QuestObjectiveMarker() {
    override fun draw(graphics: Graphics) {
        val xs = boundary.points.map { it.x.toInt() }.toIntArray()
        val ys = boundary.points.map { it.y.toInt() }.toIntArray()
        graphics.fillPolygon(xs, ys, boundary.points.size)
    }
}

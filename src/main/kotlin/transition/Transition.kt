package com.shadowforgedmmo.engine.transition

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.entity.Hologram
import com.shadowforgedmmo.engine.gameobject.GameObject
import com.shadowforgedmmo.engine.gameobject.GameObjectSpawner
import com.shadowforgedmmo.engine.instance.Instance
import com.shadowforgedmmo.engine.instance.InstanceReference
import com.shadowforgedmmo.engine.math.Position
import com.shadowforgedmmo.engine.math.Vector3
import com.shadowforgedmmo.engine.runtime.Runtime
import com.shadowforgedmmo.engine.util.toMinestom
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType

class Transition(
    spawner: TransitionSpawner,
    instance: Instance,
    runtime: Runtime
) : GameObject(spawner, instance, runtime) {
    override val entity = Entity(EntityType.ARMOR_STAND)
    private val zoneNameHologram = Hologram()
    private val zoneLevelHologram = Hologram()

    private val toInstance
        get() = (spawner as TransitionSpawner).toInstanceReference.resolve(runtime.resources.instanceRegistry)

    private val toPosition
        get() = (spawner as TransitionSpawner).toPosition

    init {
        setBoundingBox(spawner.halfExtents)
    }

    override fun spawn() {
        val toZone = toInstance.zoneAt(toPosition.toVector2()) ?: error("No zone at $position")
        zoneNameHologram.text = toZone.displayName
        zoneLevelHologram.text = toZone.levelText
        val boundingBox = boundingBox
        zoneNameHologram.setInstance(
            instance.instanceContainer,
            boundingBox.center.toMinestom()
        )
        zoneLevelHologram.setInstance(
            instance.instanceContainer,
            (boundingBox.center + Vector3.DOWN * 0.4).toMinestom()
        )
    }

    override fun tick() {
        getOverlappingObjects<PlayerCharacter>().forEach {
            it.teleport(toInstance, toPosition)
        }
    }

    override fun despawn() {
        zoneNameHologram.remove()
        zoneLevelHologram.remove()
    }
}

class TransitionSpawner(
    position: Position,
    val halfExtents: Vector3,
    val toInstanceReference: InstanceReference,
    val toPosition: Position
) : GameObjectSpawner(position) {
    override fun spawn(instance: Instance, runtime: Runtime) =
        Transition(this, instance, runtime)
}

data class TransitionDefinition(
    @JsonProperty("position") val position: Position,
    @JsonProperty("size") val size: Vector3,
    @JsonProperty("to_instance") val toInstanceReference: InstanceReference,
    @JsonProperty("to_position") val toPosition: Position
) {
    fun toTransitionSpawner() = TransitionSpawner(
        position,
        size / 2.0,
        toInstanceReference,
        toPosition
    )
}

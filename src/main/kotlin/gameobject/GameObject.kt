package com.shadowforgedmmo.engine.gameobject

import com.shadowforgedmmo.engine.character.ANIMATION_SWING_MAIN_HAND
import com.shadowforgedmmo.engine.character.ANIMATION_SWING_OFF_HAND
import com.shadowforgedmmo.engine.character.Character
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.instance.Instance
import com.shadowforgedmmo.engine.math.BoundingBox3
import com.shadowforgedmmo.engine.math.Position
import com.shadowforgedmmo.engine.math.Vector3
import com.shadowforgedmmo.engine.runtime.Runtime
import com.shadowforgedmmo.engine.util.fromMinestom
import com.shadowforgedmmo.engine.util.toMinestom
import net.kyori.adventure.sound.Sound
import net.minestom.server.collision.BoundingBox
import net.minestom.server.entity.Entity
import net.minestom.server.entity.LivingEntity
import net.minestom.server.tag.Tag
import team.unnamed.hephaestus.minestom.MinestomModelEngine
import team.unnamed.hephaestus.minestom.ModelEntity

val OBJECT_TAG = Tag.Transient<GameObject>("object")

abstract class GameObject(
    val spawner: GameObjectSpawner, // TODO: should this be abstract like entity? Probably
    instance: Instance,
    val runtime: Runtime
) {
    companion object {
        fun fromEntity(entity: Entity): GameObject? = entity.getTag(OBJECT_TAG)
    }

    abstract val entity: Entity

    var instance = instance
        private set

    var position = spawner.position
        private set

    var previousPosition = spawner.position
        private set

    var velocity
        get() = Vector3.fromMinestom(entity.velocity)
        set(value) {
            entity.velocity = value.toMinestom()
        }

    val isOnGround
        get() = entity.isOnGround

    val boundingBox
        get() = BoundingBox3(
            position.toVector3() + Vector3.fromMinestom(entity.boundingBox.relativeStart()),
            position.toVector3() + Vector3.fromMinestom(entity.boundingBox.relativeEnd()),
        )

    var removed = false
        private set

    open val removeEntityOnDespawn
        get() = true

    private var entityTeleporting = false

    fun teleport(position: Position) = teleport(instance, position)

    fun teleport(instance: Instance, position: Position) {
        this.instance.objects.remove(boundingBox, this)
        entityTeleporting = true

        val onFinish = {
            entityTeleporting = false
            this.instance = instance
            this.position = position
            instance.objects.put(boundingBox, this)
        }

        if (instance == this.instance) {
            entity.teleport(position.toMinestom()).thenRun(onFinish)
        } else {
            entity.setInstance(instance.instanceContainer, position.toMinestom()).thenRun(onFinish)
        }
    }

    // TODO:  should this go in GameObject?
    fun lookAt(position: Vector3) {
        entity.lookAt(position.toMinestom())
    }

    fun lookAt(character: Character) = lookAt(character.eyePosition)

    fun playAnimation(animation: String) {
        val entity = this.entity
        if (entity is ModelEntity) {
            if (animation in entity.model().animations())
                entity.playAnimation(animation)
        } else if (entity is LivingEntity) {
            if (animation == ANIMATION_SWING_MAIN_HAND) entity.swingMainHand()
            else if (animation == ANIMATION_SWING_OFF_HAND) entity.swingOffHand()
        }
    }

    fun emitSound(sound: Sound, localOffset: Vector3 = Vector3.ZERO) {
        val globalOffset = position.localToGlobalDirection(localOffset)
        instance.playSound(position.toVector3() + globalOffset, sound)
    }

    inline fun <reified T : GameObject> getOverlappingObjects() =
        instance.getObjectsInBox<T>(boundingBox)

    open fun spawn() {
        val entity = entity
        entity.setTag(OBJECT_TAG, this)

        if (this !is PlayerCharacter) {
            entityTeleporting = true
            entity.setInstance(instance.instanceContainer, position.toMinestom()).thenRun {
                entityTeleporting = false
            }
        }

        if (entity is ModelEntity) {
            MinestomModelEngine.minestom().tracker().startGlobalTracking(entity)
        }
    }

    open fun despawn() {
        instance.objects.remove(boundingBox, this)
        entity.removeTag(OBJECT_TAG)
        if (removeEntityOnDespawn) entity.remove()
    }

    open fun tick() {
        previousPosition = position
        if (!entityTeleporting) {
            instance.objects.remove(boundingBox, this)
            position = Position.fromMinestom(entity.position)
            instance.objects.put(boundingBox, this)
        }
    }

    fun remove() {
        despawn()
        spawner.gameObject = null
        removed = true
    }

    fun setBoundingBox(halfExtents: Vector3) {
        val extents = halfExtents * 2.0
        entity.boundingBox = BoundingBox(extents.x, extents.y, extents.z)
    }

    open fun interact(pc: PlayerCharacter) = Unit
}

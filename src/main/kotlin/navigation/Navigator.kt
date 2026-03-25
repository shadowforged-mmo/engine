package com.shadowforgedmmo.engine.navigation

import com.shadowforgedmmo.engine.character.Character
import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import com.shadowforgedmmo.engine.math.Vector3
import com.shadowforgedmmo.engine.util.fromMinestom
import com.shadowforgedmmo.engine.util.toMinestom
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.pathfinding.PPath

class Navigator(private val character: NonPlayerCharacter) {
    val pathPending
        get() = navigator.state == PPath.State.CALCULATING

    val pathPosition: Vector3?
        get() = navigator.pathPosition?.let(Vector3::fromMinestom)

    val status: PathState
        get() = when (navigator.state) {
            PPath.State.CALCULATING -> TODO()
            PPath.State.FOLLOWING -> TODO()
            PPath.State.TERMINATING -> TODO()
            PPath.State.TERMINATED -> TODO()
            PPath.State.COMPUTED -> TODO()
            PPath.State.BEST_EFFORT -> TODO()
            PPath.State.INVALID -> TODO()
        }

    private val navigator
        get() = character.entity.navigator

    private var lastStepTimeMillis = 0L

    fun setPathTo(target: Vector3, speed: Double): Boolean {
        character.entity.getAttribute(Attribute.MOVEMENT_SPEED).baseValue = speed / 20.0
        return navigator.setPathTo(target.toMinestom())
    }

    fun reset() = navigator.setPathTo(null)

    fun tick() {
        val stepSound = character.blueprint.stepSound
        if (!navigator.isComplete && character.isOnGround && stepSound != null) {
            val timeMillis = character.runtime.timeMillis
            if (timeMillis - lastStepTimeMillis > 600) { // TODO: delay should depend on speed
                character.emitSound(stepSound)
                lastStepTimeMillis = timeMillis
            }
        }
        if (navigator.state == PPath.State.FOLLOWING) spreadOut()
    }

    fun spreadOut() {
        var netForce = Vector3.ZERO
        character.instance.getObjectsInBox<Character>(character.boundingBox.expand(Vector3.ONE))
            .filterNot { it == character }.forEach {
                val offset = character.position.toVector3() - it.position.toVector3()
                val force = offset.normalized / offset.magnitude.coerceAtLeast(0.5) * character.mass * 5.0
                netForce += force
            }
        character.applyImpulse(netForce)
    }
}

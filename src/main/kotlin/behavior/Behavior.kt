package com.shadowforgedmmo.engine.behavior

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.behavior.composite.*
import com.shadowforgedmmo.engine.behavior.decorator.*
import com.shadowforgedmmo.engine.behavior.task.*
import com.shadowforgedmmo.engine.character.NonPlayerCharacter

abstract class Behavior {
    var status: BehaviorStatus = BehaviorStatus.UNINITIALIZED
        private set

    fun tick(character: NonPlayerCharacter): BehaviorStatus {
        if (status != BehaviorStatus.RUNNING) start(character)
        status = update(character)
        if (status != BehaviorStatus.RUNNING) stop(character)
        return status
    }

    fun abort(character: NonPlayerCharacter) {
        status = BehaviorStatus.FAILURE
        stop(character)
    }

    protected open fun start(character: NonPlayerCharacter) = Unit

    protected abstract fun update(character: NonPlayerCharacter): BehaviorStatus

    protected open fun stop(character: NonPlayerCharacter) = Unit
}

abstract class Composite(protected val children: List<Behavior>) : Behavior() {
    override fun stop(character: NonPlayerCharacter) {
        for (child in children) {
            if (child.status == BehaviorStatus.RUNNING) {
                child.abort(character)
            }
        }
    }
}

abstract class Decorator(protected val child: Behavior) : Behavior() {
    override fun stop(character: NonPlayerCharacter) {
        if (child.status == BehaviorStatus.RUNNING) {
            child.abort(character)
        }
    }
}

abstract class Task : Behavior()

abstract class BehaviorBlueprint {
    abstract fun create(): Behavior
}

enum class BehaviorStatus {
    UNINITIALIZED,
    RUNNING,
    SUCCESS,
    FAILURE
}

fun deserializeBehaviorBlueprint(data: JsonNode): BehaviorBlueprint =
    when (data["type"].asText()) {
        "sequence" -> deserializeSequenceBlueprint(data)
        "selector" -> deserializeSelectorBlueprint(data)
        "priority_selector" -> deserializePrioritySelectorBlueprint(data)
        "simple_parallel" -> deserializeSimpleParallelBlueprint(data)
        "random_selector" -> deserializeRandomSelectorBlueprint(data)
        "inverter" -> deserializeInverterBlueprint(data)
        "loop" -> deserializeLoopBlueprint(data)
        "loop_forever" -> deserializeLoopForeverBlueprint(data)
        "with_cooldown" -> deserializeWithCooldownBlueprint(data)
        "wait" -> deserializeWaitBlueprint(data)
        "go_to_random_position" -> deserializeGoToRandomPositionBlueprint(data)
        "has_target" -> deserializeHasTargetBlueprint(data)
        "set_velocity" -> deserializeSetVelocityBlueprint(data)
        "follow_target" -> deserializeFollowTargetBlueprint(data)
        "face_target" -> deserializeFaceTargetBlueprint(data)
        "look_at_target" -> deserializeLookAtTargetBlueprint(data)
        "is_target_in_range" -> deserializeIsTargetInRangeBlueprint(data)
        "box_attack" -> deserializeBoxAttackBlueprint(data)
        "was_damaged" -> deserializeWasDamagedBlueprint(data)
        "emit_sound" -> deserializeEmitSoundBlueprint(data)
        "emit_particle" -> deserializeEmitParticleBlueprint(data)
        "play_animation" -> deserializePlayAnimationBlueprint(data)
        "follow_path" -> deserializeFollowPathBlueprint(data)
        "call_method" -> deserializeCallMethodBlueprint(data)
        else -> throw IllegalArgumentException()
    }

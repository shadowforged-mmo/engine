package com.shadowforgedmmo.engine.behavior

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
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

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SequenceDefinition::class, name = "sequence"),
    JsonSubTypes.Type(value = SelectorDefinition::class, name = "selector"),
    JsonSubTypes.Type(value = PrioritySelectorDefinition::class, name = "priority_selector"),
    JsonSubTypes.Type(value = RandomSelectorDefinition::class, name = "random_selector"),
    JsonSubTypes.Type(value = SimpleParallelDefinition::class, name = "simple_parallel"),
    JsonSubTypes.Type(value = InverterDefinition::class, name = "inverter"),
    JsonSubTypes.Type(value = LoopDefinition::class, name = "loop"),
    JsonSubTypes.Type(value = LoopForeverDefinition::class, name = "loop_forever"),
    JsonSubTypes.Type(value = WithCooldownDefinition::class, name = "with_cooldown"),
    JsonSubTypes.Type(value = BoxAttackDefinition::class, name = "box_attack"),
    JsonSubTypes.Type(value = CallMethodDefinition::class, name = "call_method"),
    JsonSubTypes.Type(value = EmitParticleDefinition::class, name = "emit_particle"),
    JsonSubTypes.Type(value = EmitSoundDefinition::class, name = "emit_sound"),
    JsonSubTypes.Type(value = FaceTargetDefinition::class, name = "face_target"),
    JsonSubTypes.Type(value = FollowPathDefinition::class, name = "follow_path"),
    JsonSubTypes.Type(value = FollowTargetDefinition::class, name = "follow_target"),
    JsonSubTypes.Type(value = GoToRandomPositionDefinition::class, name = "go_to_random_position"),
    JsonSubTypes.Type(value = HasTargetDefinition::class, name = "has_target"),
    JsonSubTypes.Type(value = IsTargetInRangeDefinition::class, name = "is_target_in_range"),
    JsonSubTypes.Type(value = LookAtTargetDefinition::class, name = "look_at_target"),
    JsonSubTypes.Type(value = PlayAnimationDefinition::class, name = "play_animation"),
    JsonSubTypes.Type(value = SetVelocityDefinition::class, name = "set_velocity"),
    JsonSubTypes.Type(value = WaitDefinition::class, name = "wait"),
    JsonSubTypes.Type(value = WasDamagedDefinition::class, name = "was_damaged")
)
sealed class BehaviorDefinition {
    abstract fun toBlueprint(): BehaviorBlueprint
}

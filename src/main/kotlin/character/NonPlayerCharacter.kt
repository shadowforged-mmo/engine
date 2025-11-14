package com.shadowforgedmmo.engine.character

import com.shadowforgedmmo.engine.ai.navigation.Navigator
import com.shadowforgedmmo.engine.combat.Damage
import com.shadowforgedmmo.engine.instance.Instance
import com.shadowforgedmmo.engine.math.Position
import com.shadowforgedmmo.engine.math.Vector3
import com.shadowforgedmmo.engine.runtime.Runtime
import com.shadowforgedmmo.engine.util.schedulerManager
import net.kyori.adventure.text.Component
import net.minestom.server.particle.Particle
import java.time.Duration
import kotlin.math.pow
import com.shadowforgedmmo.engine.script.NonPlayerCharacter as ScriptNonPlayerCharacter
import net.minestom.server.entity.damage.DamageType as MinecraftDamageType

class NonPlayerCharacter(
    spawner: NonPlayerCharacterSpawner,
    instance: Instance,
    runtime: Runtime
) : Character(spawner, instance, runtime, spawner.summoner) {
    val blueprint
        get() = (spawner as NonPlayerCharacterSpawner).blueprint
    override val entity = blueprint.model.createEntity()
    override val name
        get() = blueprint.name
    override val level
        get() = blueprint.level
    override val maxHealth
        get() = blueprint.maxHealth
    override var health = maxHealth
    override val mass
        get() = blueprint.mass
    val navigator = Navigator(this)
    val behavior = blueprint.behavior?.create()
    var target: Character? = null
    private val bossFight = blueprint.bossFight?.create(this)
    private val interactionIndices = mutableMapOf<Pair<PlayerCharacter, Interaction>, Int>()
    private val attackers = mutableSetOf<PlayerCharacter>()
    override val handle = blueprint.scriptId?.let {
        runtime.interpreter.instantiate(it, this)
    } ?: ScriptNonPlayerCharacter(this)

    override fun spawn() {
        super.spawn()
        if (entity is BlockbenchCharacterModelEntity) entity.spawnHitbox()
        bossFight?.init()
        handle.on_spawn()
    }

    override fun despawn() {
        super.despawn()
        if (entity is BlockbenchCharacterModelEntity) entity.removeHitbox()
        bossFight?.remove()
        handle.on_despawn()
    }

    override fun tick() {
        super.tick()
        updateTarget()
        if (isAlive and interactionIndices.isEmpty()) {
            navigator.tick()
            behavior?.tick(this)
        }
        bossFight?.tick()
        handle.tick()
    }

    private fun updateTarget() {
        // TODO: implement threat
        val aggroRadius = 25.0 // TODO
        val prevTarget = target
        if (prevTarget == null || !prevTarget.isAlive || Position.sqrDistance(
                position,
                prevTarget.position
            ) > aggroRadius.pow(2)
        ) {
            target = instance.getNearbyObjects<Character>(
                position.toVector3(),
                aggroRadius
            ).filter(::shouldTarget).minByOrNull { Position.sqrDistance(position, it.position) }
        }
    }

    private fun shouldTarget(character: Character) = character !== this &&
            character.isAlive &&
            getStance(character) === Stance.HOSTILE &&
            !character.isInvisible

    override fun interact(pc: PlayerCharacter) {
        if (getStance(pc) == Stance.HOSTILE) return

        val availableInteractions = blueprint.interactions.filter {
            it.isAvailable(pc)
        }
        val interaction = availableInteractions.firstOrNull()

        if (interaction == null) {
            blueprint.speakSound?.let { pc.playSound(it, position.toVector3()) }
            return
        }

        val index = interactionIndices.getOrDefault(pc to interaction, 0)
        lookAt(pc)
        if (interaction.advance(this, pc, index)) {
            interactionIndices[pc to interaction] = index + 1
            pc.disableMovement()
        } else {
            interactionIndices.remove(pc to interaction)
            pc.enableMovement()
        }
    }

    override fun speak(dialogue: Component, to: PlayerCharacter) {
        super.speak(dialogue, to)
        blueprint.speakSound?.let { to.playSound(it, position.toVector3()) }
    }

    override fun getStance(toward: Character) = blueprint.stances.stance(toward)

    override fun damage(damage: Damage, source: Character) {
        if (source is PlayerCharacter) attackers.add(source)
        super.damage(damage, source)
        takeDamageEffect()
    }

    private fun takeDamageEffect() {
        if (entity is BlockbenchCharacterModelEntity) {
            // TODO
        } else {
            entity.damage(MinecraftDamageType.GENERIC, 0.0F)
        }
    }

    override fun die() {
        navigator.reset()
        if (entity is BlockbenchCharacterModelEntity) entity.animationPlayer().clear()
        playAnimation(ANIMATION_DEATH)
        blueprint.deathSound?.let(::emitSound)
        attackers.forEach { runtime.questObjectiveManager.handleCharacterDeath(it, this) }
        schedulerManager.buildTask(::finalizeDeath)
            .delay(Duration.ofMillis(blueprint.removalDelayMillis))
            .schedule()
    }

    private fun distributeExperiencePoints() {
        if (attackers.isEmpty()) return
        val experiencePointsEach = blueprint.experiencePoints / attackers.size
        if (experiencePointsEach == 0) return
        val experiencePointsPosition = position.toVector3() + Vector3.UP * height / 2.0
        attackers.forEach { it.addExperiencePoints(experiencePointsEach, experiencePointsPosition) }
    }

    private fun finalizeDeath() {
        // TODO: loot
        distributeExperiencePoints()
        spawner.spawnAfterMillis = runtime.timeMillis + blueprint.respawnTimeMillis
        remove()
        spawnDeathParticles()
    }

    private fun spawnDeathParticles() = instance.spawnParticle(
        position.toVector3() - Vector3.ONE / 4.0 + Vector3.UP,
        Particle.POOF,
        offset = Vector3.ONE / 2.0,
        maxSpeed = 0.1,
        count = 10
    )
}

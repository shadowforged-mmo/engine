package com.shadowforgedmmo.engine.behavior

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import com.shadowforgedmmo.engine.math.Vector3
import net.minestom.server.particle.Particle

class EmitParticle(
    private val particle: Particle,
    private val offset: Vector3
) : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val position = character.position.toVector3() + character.position.localToGlobalDirection(offset)
        character.instance.spawnParticle(position, particle)
        return BehaviorStatus.SUCCESS
    }
}

class EmitParticleBlueprint(
    private val particle: Particle,
    private val offset: Vector3
) : BehaviorBlueprint() {
    override fun create() = EmitParticle(particle, offset)
}

data class EmitParticleDefinition(
    @JsonProperty("particle") val particle: Particle,
    @JsonProperty("offset") val offset: Vector3? = null
) : BehaviorDefinition() {
    override fun toBlueprint() =
        EmitParticleBlueprint(particle, offset ?: Vector3.ZERO)
}

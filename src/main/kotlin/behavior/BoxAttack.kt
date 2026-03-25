package com.shadowforgedmmo.engine.behavior

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.Character
import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import com.shadowforgedmmo.engine.character.Stance
import com.shadowforgedmmo.engine.combat.Damage
import com.shadowforgedmmo.engine.math.BoundingBox3
import com.shadowforgedmmo.engine.math.Vector3
import com.shadowforgedmmo.engine.sound.SoundDefinition
import net.kyori.adventure.sound.Sound

class BoxAttack(
    private val damage: Damage,
    private val offset: Vector3,
    private val width: Double,
    private val height: Double,
    private val knockback: Vector3,
    private val hitSound: Sound?,
    private val missSound: Sound?
) : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val center = character.position.toVector3() + character.position.localToGlobalDirection(offset)
        val halfExtents = Vector3(width, height, width) / 2.0
        val hits = character.instance.getObjectsInBox<Character>(BoundingBox3.from(center, halfExtents))
            .filter { it != character && it.isAlive && character.getStance(it) == Stance.HOSTILE }

        if (hits.isEmpty()) {
            missSound?.let { character.instance.playSound(center, it) }
        } else {
            hitSound?.let { character.instance.playSound(center, it) }

            val globalKnockback = character.position.localToGlobalDirection(knockback)
            hits.forEach {
                it.damage(damage, character)
                it.applyImpulse(globalKnockback)
            }
        }

        return BehaviorStatus.SUCCESS
    }
}

class BoxAttackBlueprint(
    private val damage: Damage,
    private val offset: Vector3,
    private val width: Double,
    private val height: Double,
    private val knockback: Vector3,
    private val hitSound: Sound?,
    private val missSound: Sound?
) : BehaviorBlueprint() {
    override fun create() = BoxAttack(
        damage,
        offset,
        width,
        height,
        knockback,
        hitSound,
        missSound
    )
}

data class BoxAttackDefinition(
    @JsonProperty("damage") val damage: Damage,
    @JsonProperty("offset") val offset: Vector3,
    @JsonProperty("width") val width: Double,
    @JsonProperty("height") val height: Double,
    @JsonProperty("knockback") val knockback: Vector3,
    @JsonProperty("hit_sound") val hitSound: SoundDefinition?,
    @JsonProperty("miss_sound") val missSound: SoundDefinition?
) : BehaviorDefinition() {
    override fun toBlueprint() =
        BoxAttackBlueprint(damage, offset, width, height, knockback, hitSound?.toSound(), missSound?.toSound())
}

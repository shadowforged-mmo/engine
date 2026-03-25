package com.shadowforgedmmo.engine.behavior

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import com.shadowforgedmmo.engine.math.Vector3
import net.kyori.adventure.sound.Sound

class EmitSound(
    private val sound: Sound,
    private val offset: Vector3
) : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        character.emitSound(sound, offset)
        return BehaviorStatus.SUCCESS
    }
}

class EmitSoundBlueprint(
    private val sound: Sound,
    private val offset: Vector3
) : BehaviorBlueprint() {
    override fun create() = EmitSound(sound, offset)
}

data class EmitSoundDefinition(
    @JsonProperty("sound") val sound: Sound,
    @JsonProperty("offset") val offset: Vector3? = null
) : BehaviorDefinition() {
    override fun toBlueprint() =
        EmitSoundBlueprint(sound, offset ?: Vector3.ZERO)
}

package com.shadowforgedmmo.engine.sound

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.shadowforgedmmo.engine.behavior.SelectorDefinition
import com.shadowforgedmmo.engine.behavior.SequenceDefinition
import com.shadowforgedmmo.engine.minecraft.MinecraftReference
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SequenceDefinition::class, name = "sequence"),
    JsonSubTypes.Type(value = SelectorDefinition::class, name = "selector")
)
sealed class SoundDefinition {
    abstract fun toSound(): Sound
}

data class MinecraftSoundDefinition(
    @JsonProperty("name") val name: MinecraftReference,
    @JsonProperty("volume") val volume: Float,
    @JsonProperty("pitch") val pitch: Float
) : SoundDefinition() {
    override fun toSound() = Sound.sound(
        Key.key(TODO()),
        Sound.Source.MASTER,
        volume,
        pitch
    )
}

data class SoundAssetSoundDefinition(
    @JsonProperty("name") val name: SoundAssetReference,
    @JsonProperty("volume") val volume: Float,
    @JsonProperty("pitch") val pitch: Float
) : SoundDefinition() {
    override fun toSound() = TODO()
}

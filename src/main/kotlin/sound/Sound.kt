package com.shadowforgedmmo.engine.sound

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.shadowforgedmmo.engine.minecraft.MinecraftReference
import com.shadowforgedmmo.engine.resource.MINECRAFT
import com.shadowforgedmmo.engine.resource.SOUNDS
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = MinecraftSoundDefinition::class, name = "minecraft"),
    JsonSubTypes.Type(value = SoundAssetSoundDefinition::class, name = "sound_asset")
)
sealed class SoundDefinition {
    abstract fun toSound(): Sound
}

data class MinecraftSoundDefinition(
    @JsonProperty("name") val name: MinecraftReference,
    @JsonProperty("source") val source: Sound.Source?,
    @JsonProperty("volume") val volume: Float?,
    @JsonProperty("pitch") val pitch: Float?
) : SoundDefinition() {
    override fun toSound() = Sound.sound(
        Key.key(MINECRAFT, name.id),
        source ?: Sound.Source.MASTER,
        volume ?: 1.0F,
        pitch ?: 1.0F
    )
}

data class SoundAssetSoundDefinition(
    @JsonProperty("name") val name: SoundAssetReference,
    @JsonProperty("source") val source: Sound.Source?,
    @JsonProperty("volume") val volume: Float?,
    @JsonProperty("pitch") val pitch: Float?
) : SoundDefinition() {
    override fun toSound() = Sound.sound(
        Key.key(SOUNDS, name.id),
        source ?: Sound.Source.MASTER,
        volume ?: 1.0F,
        pitch ?: 1.0F
    )
}

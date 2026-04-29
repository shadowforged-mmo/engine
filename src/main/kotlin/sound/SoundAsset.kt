package com.shadowforgedmmo.engine.sound

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.pack.Namespaces
import com.shadowforgedmmo.engine.resource.ResourceReference
import com.shadowforgedmmo.engine.resource.ResourceReferenceDeserializer
import com.shadowforgedmmo.engine.resource.SOUNDS
import net.kyori.adventure.key.Key
import java.io.File

class SoundAsset(val id: String, val file: File) {
    val key = soundKey(id)
}

@JsonDeserialize(using = SoundAssetDeserializer::class)
class SoundAssetReference(id: String) : ResourceReference(id)

class SoundAssetDeserializer : ResourceReferenceDeserializer<SoundAssetReference>(
    SOUNDS,
    ::SoundAssetReference
)

private fun soundKey(id: String) = Key.key(Namespaces.SOUNDS, id)

package com.shadowforgedmmo.engine.music

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.pack.Namespaces
import com.shadowforgedmmo.engine.resource.MUSIC
import com.shadowforgedmmo.engine.resource.ResourceReference
import com.shadowforgedmmo.engine.resource.ResourceReferenceDeserializer
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import java.io.File
import java.time.Duration

class MusicTrack(val id: String, val duration: Duration) {
    val sound = Sound.sound(
        songKey(id),
        Sound.Source.MUSIC,
        1.0F,
        1.0F
    )
}

class MusicTrackDefinition(val duration: Duration) {
    fun toMusicTrack(id: String) = MusicTrack(id, duration)
}

class MusicTrackAsset(val id: String, val file: File) {
    val key = songKey(id)
}

@JsonDeserialize(using = SongReferenceDeserializer::class)
class SongReference(id: String) : ResourceReference(id)

class SongReferenceDeserializer : ResourceReferenceDeserializer<SongReference>(
    MUSIC,
    ::SongReference
)

private fun songKey(id: String) = Key.key(Namespaces.MUSIC, id)

// TODO: remove this
fun deserializeSongAsset(id: String, file: File) = MusicTrackAsset(id, file)


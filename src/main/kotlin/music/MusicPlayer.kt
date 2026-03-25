package com.shadowforgedmmo.engine.music

import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.util.schedulerManager
import net.minestom.server.timer.Task
import java.time.Duration

class MusicPlayer(private val pc: PlayerCharacter) {
    private var musicTrack: MusicTrack? = null
    private var replayTask: Task? = null

    fun setSong(musicTrack: MusicTrack?) {
        if (musicTrack == this.musicTrack) return

        replayTask?.cancel()
        this.musicTrack?.let { pc.entity.stopSound(it.sound) }

        musicTrack?.let {
            replayTask = schedulerManager.buildTask {
                pc.entity.playSound(it.sound)
            }.repeat(it.duration).schedule()
        }
        this.musicTrack = musicTrack
    }
}

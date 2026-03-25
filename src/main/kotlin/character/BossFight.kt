package com.shadowforgedmmo.engine.character

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.math.Position
import com.shadowforgedmmo.engine.music.MusicTrack
import com.shadowforgedmmo.engine.music.SongReference
import com.shadowforgedmmo.engine.resource.Registry
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import kotlin.math.pow

private const val LEAVE_OFFSET = 5.0

class BossFight(
    val character: NonPlayerCharacter,
    val radius: Double,
    val music: MusicTrack
) {
    private val viewers = mutableSetOf<PlayerCharacter>()
    private val bossBar = BossBar.bossBar(
        Component.empty(),
        1.0F,
        BossBar.Color.RED,
        BossBar.Overlay.NOTCHED_12
    )

    fun init() {
        bossBar.name(character.displayNameWithLevel(Stance.HOSTILE))
    }

    fun tick() {
        bossBar.progress((character.health.toFloat() / character.maxHealth.toFloat()))

        viewers.filter {
            it.removed || Position.sqrDistance(
                it.position,
                character.position
            ) > (radius + LEAVE_OFFSET).pow(2)
        }.forEach(::removeViewer)

        character.instance
            .getNearbyObjects<PlayerCharacter>(character.position.toVector3(), radius)
            .minus(viewers)
            .filter { character.getStance(it) == Stance.HOSTILE }
            .forEach(::addViewer)
    }

    private fun addViewer(viewer: PlayerCharacter) {
        viewers.add(viewer)
        bossBar.addViewer(viewer.entity)
        viewer.bossFights.add(this)
        viewer.updateMusic()
    }

    private fun removeViewer(viewer: PlayerCharacter) {
        viewers.remove(viewer)
        bossBar.removeViewer(viewer.entity)
        viewer.bossFights.remove(this)
        viewer.updateMusic()
    }

    fun remove() {
        viewers.toList().forEach(::removeViewer)
    }
}

class BossFightBlueprint(
    private val radius: Double,
    private val music: MusicTrack
) {
    fun create(character: NonPlayerCharacter) = BossFight(character, radius, music)
}

class BossFightDefinition(
    @JsonProperty("radius") val radius: Double,
    @JsonProperty("music") val songReference: SongReference
) {
    fun toBossFightBlueprint(musicRegistry: Registry<MusicTrack>) = BossFightBlueprint(
        radius,
        songReference.resolve(musicRegistry)
    )
}

package com.shadowforgedmmo.engine.zone

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.character.CharacterBlueprint
import com.shadowforgedmmo.engine.character.CharacterSpawnsDefinition
import com.shadowforgedmmo.engine.character.NonPlayerCharacterSpawner
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.loot.LootChestDefinition
import com.shadowforgedmmo.engine.math.Polygon
import com.shadowforgedmmo.engine.music.MusicTrack
import com.shadowforgedmmo.engine.music.SongReference
import com.shadowforgedmmo.engine.resource.Registry
import com.shadowforgedmmo.engine.resource.ResourceReference
import com.shadowforgedmmo.engine.resource.ResourceReferenceDeserializer
import com.shadowforgedmmo.engine.resource.ZONES
import com.shadowforgedmmo.engine.transition.TransitionDefinition
import com.shadowforgedmmo.engine.util.schedulerManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.time.Duration
import net.minestom.server.instance.Weather as MinestomWeather

class Zone(
    val id: String,
    val name: String,
    val type: ZoneType,
    val level: Int,
    val boundary: Polygon,
    val music: MusicTrack?,
    val weatherCycle: WeatherCycle
) {
    val outerBoundary = boundary.offset(10.0) // TOOD: update offset

    val displayName
        get() = Component.text(name, type.color)

    val levelText
        get() = Component.text("Level $level", NamedTextColor.GOLD)

    val playerCharacters: Set<PlayerCharacter> = setOf()

    var weather =
        if (weatherCycle.weatherEntries.isEmpty()) Weather.CLEAR
        else weatherCycle.weatherEntries[0].weather
        private set(value) {
            field = value
            val packets = MinestomWeather(
                weather.rain,
                weather.thunder
            ).createWeatherPackets()
            playerCharacters.forEach { it.entity.sendPackets(packets) }
        }

    fun init() {
        if (weatherCycle.weatherEntries.size > 1) scheduleWeatherUpdate(1)
    }

    private fun scheduleWeatherUpdate(index: Int) {
        schedulerManager.buildTask {
            weather = weatherCycle.weatherEntries[index].weather
            scheduleWeatherUpdate((index + 1) % weatherCycle.weatherEntries.size)
        }
            .delay(Duration.ofMillis(weatherCycle.weatherEntries[index].durationMillis))
            .schedule()
    }
}

data class ZoneDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("type") val type: ZoneType,
    @JsonProperty("level") val level: Int,
    @JsonProperty("boundary") val boundary: Polygon,
    @JsonProperty("music") val songReference: SongReference?,
    @JsonProperty("weather_cycle") val weatherCycle: WeatherCycle,
    @JsonProperty("transitions") val transitions: List<TransitionDefinition>,
    @JsonProperty("characters") val characters: List<CharacterSpawnsDefinition>,
    @JsonProperty("loot_chests") val lootChests: List<LootChestDefinition>
) {
    fun toZone(id: String, musicTrackRegistry: Registry<MusicTrack>) = Zone(
        id,
        name,
        type,
        level,
        boundary,
        songReference?.resolve(musicTrackRegistry),
        weatherCycle
    )

    fun getSpawners(characterBlueprintRegistry: Registry<CharacterBlueprint>) =
        getTransitionSpawners() + getCharacterSpawners(characterBlueprintRegistry)

    private fun getTransitionSpawners() = transitions.map { it.toTransitionSpawner() }

    private fun getCharacterSpawners(
        characterBlueprintRegistry: Registry<CharacterBlueprint>
    ): Collection<NonPlayerCharacterSpawner> =
        characters.flatMap { it.toCharacterSpawners(characterBlueprintRegistry) }
}

@JsonDeserialize(using = ZoneReferenceDeserializer::class)
class ZoneReference(id: String) : ResourceReference(id)

class ZoneReferenceDeserializer : ResourceReferenceDeserializer<ZoneReference>(
    ZONES,
    ::ZoneReference
)

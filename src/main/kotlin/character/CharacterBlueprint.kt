package com.shadowforgedmmo.engine.character

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.behavior.BehaviorBlueprint
import com.shadowforgedmmo.engine.behavior.BehaviorDefinition
import com.shadowforgedmmo.engine.loot.LootTable
import com.shadowforgedmmo.engine.loot.LootTableDefinition
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.model.BlockbenchModel
import com.shadowforgedmmo.engine.model.Skin
import com.shadowforgedmmo.engine.music.MusicTrack
import com.shadowforgedmmo.engine.quest.Quest
import com.shadowforgedmmo.engine.resource.CHARACTERS
import com.shadowforgedmmo.engine.resource.Registry
import com.shadowforgedmmo.engine.resource.ResourceReference
import com.shadowforgedmmo.engine.resource.ResourceReferenceDeserializer
import com.shadowforgedmmo.engine.script.Script
import com.shadowforgedmmo.engine.script.ScriptReference
import com.shadowforgedmmo.engine.sound.SoundDefinition
import com.shadowforgedmmo.engine.time.secondsToMillis
import net.kyori.adventure.sound.Sound

class CharacterBlueprint(
    val id: String,
    val name: String,
    val level: Int,
    val maxHealth: Int,
    val mass: Double,
    val experiencePoints: Int,
    val model: CharacterModel,
    val bossFight: BossFightBlueprint?,
    val behavior: BehaviorBlueprint?,
    val stances: Stances,
    val stepSound: Sound?,
    val speakSound: Sound?,
    val hurtSound: Sound?,
    val deathSound: Sound?,
    val interactions: List<Interaction>,
    val lootTable: LootTable?,
    val script: Script?,
    val removalDelayMillis: Long,
    val respawnTimeMillis: Long
)

data class CharacterBlueprintDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("level") val level: Int,
    @JsonProperty("max_health") val maxHealth: Int?,
    @JsonProperty("mass") val mass: Double?,
    @JsonProperty("experience_points") val experiencePoints: Int?,
    @JsonProperty("model") val model: CharacterModelDefinition,
    @JsonProperty("boss_fight") val bossFight: BossFightDefinition?,
    @JsonProperty("behavior") val behavior: BehaviorDefinition?,
    @JsonProperty("stance") val stances: StancesDefinition?,
    @JsonProperty("step_sound") val stepSound: SoundDefinition?,
    @JsonProperty("speak_sound") val speakSound: SoundDefinition?,
    @JsonProperty("hurt_sound") val hurtSound: SoundDefinition?,
    @JsonProperty("hurt_sound_cooldown") val hurtSoundCooldown: Double?,
    @JsonProperty("death_sound") val deathSound: SoundDefinition?,
    @JsonProperty("interactions") val interactions: List<InteractionDefinition>?,
    @JsonProperty("loot") val lootTable: LootTableDefinition?,
    @JsonProperty("script") val scriptReference: ScriptReference?,
    @JsonProperty("removal_delay") val removalDelaySeconds: Double?,
    @JsonProperty("respawn_time") val respawnTimeSeconds: Double?
) {
    fun toCharacterBlueprint(
        id: String,
        blockbenchModelRegistry: Registry<BlockbenchModel>,
        skinRegistry: Registry<Skin>,
        blockbenchItemModelRegistry: Registry<BlockbenchItemModel>,
        questRegistry: Registry<Quest>,
        musicRegistry: Registry<MusicTrack>,
        scriptRegistry: Registry<Script>
    ) = CharacterBlueprint(
        id,
        name,
        level,
        maxHealth ?: 1,
        mass ?: 75.0,
        experiencePoints ?: 0,
        model.toCharacterModel(blockbenchModelRegistry, skinRegistry, blockbenchItemModelRegistry),
        bossFight?.toBossFightBlueprint(musicRegistry),
        behavior?.toBlueprint(),
        stances?.toStances() ?: Stances(),
        stepSound?.toSound(),
        speakSound?.toSound(),
        hurtSound?.toSound(),
        deathSound?.toSound(),
        interactions?.map { it.toInteraction(questRegistry) } ?: emptyList(),
        lootTable?.toLootTable(),
        scriptReference?.resolve(scriptRegistry),
        removalDelaySeconds?.let(::secondsToMillis) ?: 1000L,
        respawnTimeSeconds?.let(::secondsToMillis) ?: 300000L
    )
}

@JsonDeserialize(using = CharacterBlueprintReferenceDeserializer::class)
class CharacterBlueprintReference(id: String) : ResourceReference(id)

class CharacterBlueprintReferenceDeserializer : ResourceReferenceDeserializer<CharacterBlueprintReference>(
    CHARACTERS,
    ::CharacterBlueprintReference
)

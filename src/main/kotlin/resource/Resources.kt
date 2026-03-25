package com.shadowforgedmmo.engine.resource

import com.shadowforgedmmo.engine.character.CharacterBlueprint
import com.shadowforgedmmo.engine.instance.Instance
import com.shadowforgedmmo.engine.item.Item
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.model.BlockbenchModel
import com.shadowforgedmmo.engine.music.MusicTrack
import com.shadowforgedmmo.engine.playerclass.PlayerClass
import com.shadowforgedmmo.engine.quest.Quest
import com.shadowforgedmmo.engine.runtime.RuntimeEnvironment
import com.shadowforgedmmo.engine.skill.Skill
import com.shadowforgedmmo.engine.zone.Zone
import net.minestom.server.MinecraftServer
import java.io.File

const val CONFIG = "config"
const val MUSIC = "music"
const val CHARACTERS = "characters"
const val SKILLS = "skills"
const val CLASSES = "classes"
const val INSTANCES = "instances"
const val ITEM_MODELS = "item_models"
const val ITEMS = "items"
const val QUESTS = "quests"
const val SCRIPTS = "scripts"
const val SOUNDS = "sounds"
const val WORLDS = "worlds"
const val ZONES = "zones"
const val SKINS = "skins"
const val MINECRAFT = "minecraft"
const val MODELS = "models"

class Resources(
    val server: MinecraftServer,
    val config: Config,
    val environment: RuntimeEnvironment,
    val classRegistry: Registry<PlayerClass>,
    val skillRegistry: Registry<Skill>,
    val itemRegistry: Registry<Item>,
    val instanceRegistry: Registry<Instance>,
    val questRegistry: Registry<Quest>,
    val musicTrackRegistry: Registry<MusicTrack>,
    val blockbenchModelRegistry: Registry<BlockbenchModel>,
    val blockbenchItemModelRegistry: Registry<BlockbenchItemModel>,
    val characterBlueprintRegistry: Registry<CharacterBlueprint>,
    val zoneRegistry: Registry<Zone>,
    val scriptDir: File
)

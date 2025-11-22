package com.shadowforgedmmo.engine.resource

import com.shadowforgedmmo.engine.character.CharacterBlueprint
import com.shadowforgedmmo.engine.instance.Instance
import com.shadowforgedmmo.engine.item.Item
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.model.BlockbenchModel
import com.shadowforgedmmo.engine.music.Song
import com.shadowforgedmmo.engine.playerclass.PlayerClass
import com.shadowforgedmmo.engine.quest.Quest
import com.shadowforgedmmo.engine.runtime.RuntimeEnvironment
import com.shadowforgedmmo.engine.skill.Skill
import com.shadowforgedmmo.engine.zone.Zone
import net.minestom.server.MinecraftServer
import java.io.File

class Resources(
    val server: MinecraftServer,
    val config: Config,
    val environment: RuntimeEnvironment,
    val playerClasses: Collection<PlayerClass>,
    val skills: Collection<Skill>,
    val items: Collection<Item>,
    val instances: Collection<Instance>,
    val quests: Collection<Quest>,
    val music: Collection<Song>,
    val blockbenchModels: Collection<BlockbenchModel>,
    val blockbenchItemModels: Collection<BlockbenchItemModel>,
    val characterBlueprints: Collection<CharacterBlueprint>,
    val zones: Collection<Zone>,
    val scriptDir: File
)

package com.shadowforgedmmo.engine.resource

import com.shadowforgedmmo.engine.character.CharacterBlueprintDefinition
import com.shadowforgedmmo.engine.icon.IconDefinition
import com.shadowforgedmmo.engine.instance.InstanceDefinition
import com.shadowforgedmmo.engine.item.ItemDefinition
import com.shadowforgedmmo.engine.model.BlockbenchItemModelDefinition
import com.shadowforgedmmo.engine.model.BlockbenchModelDefinition
import com.shadowforgedmmo.engine.model.SkinDefinition
import com.shadowforgedmmo.engine.music.MusicTrackDefinition
import com.shadowforgedmmo.engine.playerclass.PlayerClassDefinition
import com.shadowforgedmmo.engine.quest.QuestDefinition
import com.shadowforgedmmo.engine.script.ScriptDefinition
import com.shadowforgedmmo.engine.skill.SkillDefinition
import com.shadowforgedmmo.engine.time.secondsToDuration
import com.shadowforgedmmo.engine.util.readYaml
import com.shadowforgedmmo.engine.world.WorldDefinition
import com.shadowforgedmmo.engine.zone.ZoneDefinition
import org.gagravarr.ogg.audio.OggAudioStatistics
import org.gagravarr.vorbis.VorbisFile
import team.unnamed.hephaestus.reader.blockbench.BBModelReader
import java.io.File
import kotlin.reflect.KClass

data class Definitions(
    val config: Config,
    val characterBlueprints: Registry<CharacterBlueprintDefinition>,
    val classes: Registry<PlayerClassDefinition>,
    val worlds: Registry<WorldDefinition>,
    val zones: Registry<ZoneDefinition>,
    val instances: Registry<InstanceDefinition>,
    val items: Registry<ItemDefinition>,
    val quests: Registry<QuestDefinition>,
    val skills: Registry<SkillDefinition>,
    val musicTracks: Registry<MusicTrackDefinition>,
    val scripts: Registry<ScriptDefinition>,
    val skins: Registry<SkinDefinition>,
    val icons: Registry<IconDefinition>,
    val blockbenchModels: Registry<BlockbenchModelDefinition>,
    val blockbenchItemModels: Registry<BlockbenchItemModelDefinition>,
    val scriptDir: File
)

class DefinitionLoader(private val root: File) {
    fun loadAll() = Definitions(
        loadConfig(),
        loadCharacterBlueprints(),
        loadPlayerClasses(),
        loadWorlds(),
        loadZones(),
        loadInstances(),
        loadItems(),
        loadQuests(),
        loadSkills(),
        loadMusicTracks(),
        loadScripts(),
        loadSkins(),
        loadIcons(),
        loadBlockbenchModels(),
        loadBlockbenchItemModels(),
        root.resolve(SCRIPTS)
    )

    fun loadConfig() = loadYamlResource(CONFIG, Config::class)

    fun loadCharacterBlueprints() = loadYamlResources(CHARACTERS, CharacterBlueprintDefinition::class)

    fun loadPlayerClasses() = loadYamlResources(CLASSES, PlayerClassDefinition::class)

    fun loadWorlds(): Registry<WorldDefinition> {
        val dir = root.resolve(WORLDS)
        return dir.walk().filter { file ->
            file.isDirectory() && file.resolve("region").exists()
        }.associate { file ->
            val id = fileToId(file, dir)
            id to WorldDefinition(file.path)
        }
    }

    fun loadZones() = loadYamlResources(ZONES, ZoneDefinition::class)

    fun loadInstances() = loadYamlResources(INSTANCES, InstanceDefinition::class)

    fun loadItems() = loadYamlResources(ITEMS, ItemDefinition::class)

    fun loadQuests() = loadYamlResources(QUESTS, QuestDefinition::class)

    fun loadSkills() = loadYamlResources(SKILLS, SkillDefinition::class)

    fun loadMusicTracks() = loadResources(MUSIC) { file ->
        MusicTrackDefinition(computeDuration(file), file)
    }

    fun loadScripts() = loadResources(SCRIPTS) { file ->
        ScriptDefinition()
    }

    fun loadSkins() = loadYamlResources(SKINS, SkinDefinition::class)

    fun loadBlockbenchModels() = with(BBModelReader.blockbench()) {
        loadResources(MODELS) { file -> BlockbenchModelDefinition(read(file)) }
    }

    fun loadBlockbenchItemModels() = with(BBModelReader.blockbench()) {
        loadResources(ITEM_MODELS) { file -> BlockbenchItemModelDefinition(read(file)) }
    }

    fun loadSounds() = loadResources(SOUNDS) { file -> file }

    private fun computeDuration(file: File) = secondsToDuration(
        VorbisFile(file).use { vorbisFile ->
            val stats = OggAudioStatistics(vorbisFile, vorbisFile)
            stats.calculate()
            stats.durationSeconds
        }
    )

    fun loadIcons() = loadResources(ICONS) { file -> IconDefinition(file) }

    private fun <T : Any> loadYamlResources(relativePath: String, classOfT: KClass<T>) =
        loadYamlResources(root.resolve(relativePath), classOfT)

    private fun <T : Any> loadYamlResources(dir: File, classOfT: KClass<T>): Registry<T> =
        loadResources(dir) { file -> readYaml(file, classOfT) }

    private fun <T> loadResources(relativePath: String, loader: (File) -> T) =
        loadResources(root.resolve(relativePath), loader)

    private fun <T> loadResources(dir: File, loader: (File) -> T): Registry<T> = dir
        .walk()
        .filter(File::isFile)
        .sorted()
        .mapNotNull { file ->
            val id = fileToId(file, dir)
            val resource = try {
                loader(file)
            } catch (e: Exception) {
                val prefix = dir.relativeTo(root).path
                // TODO: use logger
                System.err.println("Error loading resource $prefix:$id: ${e.message}")
                null
            }
            resource?.let { id to it }
        }
        .toMap()

    private fun <T : Any> loadYamlResource(relativePath: String, classOfT: KClass<T>): T =
        loadYamlResource(root.resolve("$relativePath.yaml"), classOfT)

    private fun <T : Any> loadYamlResource(file: File, classOfT: KClass<T>): T =
        loadResource(file) { file -> readYaml(file, classOfT) }

    private fun <T> loadResource(file: File, loader: (File) -> T) = loader(file)

    private fun fileToId(file: File, dir: File) =
        file.relativeTo(dir).path.substringBefore(".").replace(File.separatorChar, '.')
}

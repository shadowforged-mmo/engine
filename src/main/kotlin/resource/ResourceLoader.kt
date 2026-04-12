package com.shadowforgedmmo.engine.resource

import com.shadowforgedmmo.engine.icon.IconAsset
import com.shadowforgedmmo.engine.item.ConsumableItemDefinition
import com.shadowforgedmmo.engine.runtime.createRuntimeEnvironment
import com.shadowforgedmmo.engine.skill.ActiveSkill
import com.shadowforgedmmo.engine.skill.ActiveSkillDefinition
import com.shadowforgedmmo.engine.skill.SkillDefinition
import com.shadowforgedmmo.engine.sound.SoundAsset
import net.minestom.server.MinecraftServer

class ResourceLoader(private val definitionLoader: DefinitionLoader) {
    fun loadAll(): Resources {
        val definitions = definitionLoader.loadAll()
        val server = MinecraftServer.init()
        val config = definitions.config
        val environment = createRuntimeEnvironment()
        val worldRegistry = definitions.worlds.mapValues { (id, worldDefinition) ->
            worldDefinition.toWorld(id)
        }
        val questRegistry = definitions.quests.mapValues { (id, questDefinition) ->
            questDefinition.toQuest(id)
        }
        val musicTrackRegistry = definitions.musicTracks.mapValues { (id, musicTrackDefinition) ->
            musicTrackDefinition.toMusicTrack(id)
        }
        val blockbenchModelRegistry = definitions.blockbenchModels.mapValues { (id, blockbenchModelDefinition) ->
            blockbenchModelDefinition.toBlockbenchModel(id)
        }
        val iconRegistry = definitions.icons.mapValues { (id, iconDefinition) ->
            iconDefinition.toIcon(id)
        }
        var customModelData = 0
        val blockbenchItemModelRegistry =
            definitions.blockbenchItemModels.mapValues { (id, blockbenchItemModelDefinition) ->
                blockbenchItemModelDefinition.toBlockbenchItemModel(id, customModelData++)
            }
        val itemRegistry = definitions.items.mapValues { (id, itemDefinition) ->
            itemDefinition.toItem(id, iconRegistry, blockbenchItemModelRegistry)
        }
        val scriptRegistry = definitions.scripts.mapValues { (id, scriptDefinition) ->
            scriptDefinition.toScript(id)
        }
        val skinRegistry = definitions.skins.mapValues { (id, skinDefinition) ->
            skinDefinition.toSkin(id)
        }
        val characterBlueprintRegistry = definitions.characterBlueprints.mapValues { (id, characterDefinition) ->
            characterDefinition.toCharacterBlueprint(
                id,
                blockbenchModelRegistry,
                skinRegistry,
                blockbenchItemModelRegistry,
                questRegistry,
                musicTrackRegistry,
                scriptRegistry
            )
        }
        val skillRegistry = definitions.skills.mapValues { (id, skillDefinition) ->
            skillDefinition.toSKill(id, iconRegistry, scriptRegistry)
        }
        val playerClassRegistry = definitions.classes.mapValues { (id, playerClassDefinition) ->
            playerClassDefinition.toPlayerClass(id, skillRegistry)
        }
        val zoneRegistry = definitions.zones.mapValues { (id, zoneDefinition) ->
            zoneDefinition.toZone(id, musicTrackRegistry)
        }
        val instanceRegistry = definitions.instances.mapValues { (id, instanceDefinition) ->
            val spawners = instanceDefinition.zoneReferences
                .map { it.resolve(definitions.zones) }
                .flatMap { it.getSpawners(characterBlueprintRegistry) }
            instanceDefinition.toInstance(id, worldRegistry, zoneRegistry, spawners)
        }
        return Resources(
            server,
            config,
            environment,
            playerClassRegistry,
            skillRegistry,
            itemRegistry,
            instanceRegistry,
            questRegistry,
            musicTrackRegistry,
            blockbenchModelRegistry,
            blockbenchItemModelRegistry,
            characterBlueprintRegistry,
            zoneRegistry,
            definitions.scriptDir
        )
    }

    fun loadResourcePackResources(): ResourcePackResources {
        val config = definitionLoader.loadConfig()
        val blockbenchModels = definitionLoader.loadBlockbenchModels().mapValues { (id, modelDefinition) ->
            modelDefinition.toBlockbenchModel(id)
        }
        val blockbenchItemModelAssets = definitionLoader.loadBlockbenchItemModels().mapValues { (id, itemDefinition) ->
            itemDefinition.toBlockbenchItemModelAsset(id)
        }
        val musicTrackAssets = definitionLoader.loadMusicTracks().mapValues { (id, trackDefinition) ->
            trackDefinition.toMusicTrackAsset(id)
        }
        val soundAssets = definitionLoader.loadSounds().mapValues { (id, file) ->
            SoundAsset(id, file)
        }
//        val iconAssets = definitionLoader.loadIcons().mapValues { (id, iconDefinition) ->
//            iconDefinition.toIconAsset()
//        }
        val iconAssets = definitionLoader.loadIcons().mapValues { (id, iconDefinition) ->
            iconDefinition.toIconAsset(id)
        }
        val skillDefinitions = definitionLoader.loadSkills()
        val activeSkills = skillDefinitions.values.filterIsInstance<ActiveSkillDefinition>()
        val activeSkillIcons = activeSkills.map(ActiveSkillDefinition::iconReference)
        val itemDefinitions = definitionLoader.loadItems()
        val consumableItems = itemDefinitions.values.filterIsInstance<ConsumableItemDefinition>()
        val consumableItemIcons = consumableItems.map(ConsumableItemDefinition::iconReference)
        val iconAssetsWithCooldowns = (activeSkillIcons + consumableItemIcons)
            .map { it.resolve(iconAssets) }
            .toSet()
        return ResourcePackResources(
            config,
            blockbenchModels,
            blockbenchItemModelAssets,
            musicTrackAssets,
            soundAssets,
            iconAssets,
            iconAssetsWithCooldowns
        )
    }
}
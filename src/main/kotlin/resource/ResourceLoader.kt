package com.shadowforgedmmo.engine.resource

import com.shadowforgedmmo.engine.runtime.createRuntimeEnvironment
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
        var customModelData = 0
        val blockbenchItemModelRegistry =
            definitions.blockbenchItemModels.mapValues { (id, blockbenchItemModelDefinition) ->
                blockbenchItemModelDefinition.toBlockbenchItemModel(id, customModelData++)
            }
        val itemRegistry = definitions.items.mapValues { (id, itemDefinition) ->
            itemDefinition.toItem(id, blockbenchItemModelRegistry)
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
            skillDefinition.toSKill(id, scriptRegistry)
        }
        val playerClassRegistry = definitions.classes.mapValues { (id, playerClassDefinition) ->
            playerClassDefinition.toPlayerClass(id, skillRegistry)
        }
        val zoneRegistry = definitions.zones.mapValues { (id, zoneDefinition) ->
            zoneDefinition.toZone(id, musicTrackRegistry)
        }
        val instanceRegistry = definitions.instances.mapValues { (id, instanceDefinition) ->
            val spawners = instanceDefinition.zoneReferences
                .map { it.resolve(definitions.zones) } // TODO: need to flip reference and registry
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
        val blockbenchModels = definitionLoader.loadBlockbenchModels().map { (id, modelDefinition) ->
            modelDefinition.toBlockbenchModel(id)
        }
        val blockbenchItemModelAssets = definitionLoader.loadBlockbenchItemModels().map { (id, itemDefinition) ->

        }
        val musicTrackAssets = definitionLoader.loadMusicTracks().map { (id, trackDefinition) ->
            trackDefinition.toMusicTrackAsset(id)
        }
        val soundAssets = definitionLoader
        // TODO: consolidate some of this logic with loadAll()
        return ResourcePackResources(
            config,
            blockbenchModels,
            blockbenchItemModelAssets,
            musicTrackAssets,
            soundAssets
        )
    }
}
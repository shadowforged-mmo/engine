package com.shadowforgedmmo.engine.login

import com.shadowforgedmmo.engine.character.PlayerCharacterSpawner
import com.shadowforgedmmo.engine.persistence.deserializePlayerCharacterData
import com.shadowforgedmmo.engine.runtime.Runtime
import com.shadowforgedmmo.engine.util.globalEventHandler
import com.shadowforgedmmo.engine.util.readYaml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.world.DimensionType
import java.net.URI
import java.util.*

class LoginManager(val runtime: Runtime) {
    private val resourcePackUrl = URI(System.getenv("RESOURCE_PACK_URL"))
    private val resourcePackHash = System.getenv("RESOURCE_PACK_HASH")

    private val instanceContainer: InstanceContainer = InstanceContainer(
        UUID.randomUUID(), DimensionType.OVERWORLD
    )

    fun start() {
        MinecraftServer.getInstanceManager().registerInstance(instanceContainer)

        globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java, ::handlePlayerConfigure)
        globalEventHandler.addListener(PlayerSpawnEvent::class.java, ::handlePlayerSpawn)
    }

    private fun handlePlayerConfigure(event: AsyncPlayerConfigurationEvent) {
        event.spawningInstance = instanceContainer
        event.player.gameMode = GameMode.ADVENTURE
        event.player.sendResourcePacks(
            ResourcePackRequest.addingRequest(
                ResourcePackInfo.resourcePackInfo(
                    UUID.randomUUID(),
                    resourcePackUrl,
                    resourcePackHash
                )
            )
        )
    }

    private fun handlePlayerSpawn(event: PlayerSpawnEvent) {
        if (event.instance != instanceContainer) return

        GlobalScope.launch(Dispatchers.IO) {
            val backendUrl = "http://localhost:8001/save.json"
            val data = readYaml(URI(backendUrl).toURL())
            MinecraftServer.getSchedulerManager().scheduleNextTick {
                val playerCharacterData = deserializePlayerCharacterData(data, runtime)
                val spawner = PlayerCharacterSpawner(event.player, playerCharacterData)
                playerCharacterData.instance.spawn(spawner, runtime)
            }
        }
    }
}

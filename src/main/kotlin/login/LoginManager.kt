package com.shadowforgedmmo.engine.login

import com.shadowforgedmmo.engine.character.PlayerCharacterSpawner
import com.shadowforgedmmo.engine.persistence.deserializePlayerCharacterData
import com.shadowforgedmmo.engine.runtime.Runtime
import com.shadowforgedmmo.engine.util.globalEventHandler
import com.shadowforgedmmo.engine.util.readYaml
import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import net.minestom.server.entity.GameMode
import net.minestom.server.event.EventListener
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import java.net.URI
import java.util.*

class LoginManager(val runtime: Runtime) {
    fun start() {
        globalEventHandler.addListener(
            AsyncPlayerConfigurationEvent::class.java,
            ::handlePlayerConfigure
        )
    }

    private fun handlePlayerConfigure(event: AsyncPlayerConfigurationEvent) {
        event.player.gameMode = GameMode.ADVENTURE
        event.player.sendResourcePacks(resourcePackRequest())

        val backendUrl = "http://localhost:8001/save.json"
        val data = readYaml(URI(backendUrl).toURL())
        val playerCharacterData = deserializePlayerCharacterData(data, runtime)
        event.spawningInstance = playerCharacterData.instance.instanceContainer
        event.player.eventNode().addListener(
            EventListener.builder(PlayerSpawnEvent::class.java)
                .handler {
                    val spawner = PlayerCharacterSpawner(event.player, playerCharacterData)
                    playerCharacterData.instance.spawn(spawner, runtime)
                }
                .expireCount(1)
                .build()
        )
    }

    private fun resourcePackRequest() = ResourcePackRequest.addingRequest(
        ResourcePackInfo.resourcePackInfo(
            UUID.randomUUID(),
            URI(runtime.environment.resourcePackUrl),
            runtime.environment.resourcePackHash
        )
    )
}

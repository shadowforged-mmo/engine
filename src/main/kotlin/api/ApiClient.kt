package com.shadowforgedmmo.engine.api

import com.shadowforgedmmo.engine.character.PlayerCharacterDefinition
import com.shadowforgedmmo.engine.runtime.Runtime
import com.shadowforgedmmo.engine.util.readYaml
import java.net.URI
import java.util.*

class ApiClient(private val runtime: Runtime) {
    fun getPlayerCharacterDefinition(playerUuid: UUID): PlayerCharacterDefinition = readYaml(
        URI(runtime.resources.environment.apiUrl).toURL(),
        PlayerCharacterDefinition::class
    )
}

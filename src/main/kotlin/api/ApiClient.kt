package com.shadowforgedmmo.engine.api

import com.shadowforgedmmo.engine.character.PlayerCharacterDefinition
import com.shadowforgedmmo.engine.runtime.Runtime
import com.shadowforgedmmo.engine.util.readJson
import java.net.URI
import java.util.*

class ApiClient(private val runtime: Runtime) {
    fun getPlayerCharacterDefinition(playerUuid: UUID): PlayerCharacterDefinition = readJson(
        URI(runtime.resources.environment.apiUrl).toURL(),
        PlayerCharacterDefinition::class
    )
}

package com.shadowforgedmmo.engine.api

import com.shadowforgedmmo.engine.character.PlayerCharacterDefinition
import com.shadowforgedmmo.engine.runtime.Runtime
import java.util.*

class ApiClient(private val runtime: Runtime) {
    fun getPlayerCharacterDefinition(playerUuid: UUID): PlayerCharacterDefinition = TODO()
//        deserializePlayerCharacterData(
//        readYaml(URI(runtime.environment.apiUrl).toURL()),
//        runtime
//    )
}

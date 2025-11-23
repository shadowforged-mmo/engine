package com.shadowforgedmmo.engine.api

import com.shadowforgedmmo.engine.persistence.deserializePlayerCharacterData
import com.shadowforgedmmo.engine.runtime.Runtime
import com.shadowforgedmmo.engine.util.readYaml
import java.net.URI
import java.util.*

class Api(private val runtime: Runtime) {
    fun getCharacterData(playerUuid: UUID) = deserializePlayerCharacterData(
        readYaml(URI(runtime.environment.apiUrl).toURL()),
        runtime
    )
}

package com.shadowforgedmmo.engine.resource

import com.fasterxml.jackson.databind.JsonNode

class Config(
    val name: String
)

fun deserializeConfig(data: JsonNode): Config {
    return Config(
        data["name"].asText()
    )
}

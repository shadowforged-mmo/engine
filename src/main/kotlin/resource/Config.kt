package com.shadowforgedmmo.engine.resource

import com.fasterxml.jackson.annotation.JsonProperty

class Config(
    @JsonProperty("name") val name: String,
    @JsonProperty("resource_pack_uri") val resourcePackUri: String,
    @JsonProperty("mojang_auth") val mojangAuth: Boolean
)

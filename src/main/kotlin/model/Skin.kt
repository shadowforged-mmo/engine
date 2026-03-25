package com.shadowforgedmmo.engine.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.resource.ResourceReference
import com.shadowforgedmmo.engine.resource.ResourceReferenceDeserializer
import com.shadowforgedmmo.engine.resource.SKINS

class Skin(val id: String, val textures: String, val signature: String)

data class SkinDefinition(
    @JsonProperty("textures") val textures: String,
    @JsonProperty("signature") val signature: String
) {
    fun toSkin(id: String) = Skin(id, textures, signature)
}

@JsonDeserialize(using = SkinReferenceDeserializer::class)
class SkinReference(id: String) : ResourceReference(id)

class SkinReferenceDeserializer : ResourceReferenceDeserializer<SkinReference>(
    SKINS,
    ::SkinReference
)

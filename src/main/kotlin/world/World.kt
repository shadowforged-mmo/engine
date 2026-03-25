package com.shadowforgedmmo.engine.world

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.resource.ResourceReference
import com.shadowforgedmmo.engine.resource.ResourceReferenceDeserializer
import com.shadowforgedmmo.engine.resource.WORLDS

class World(val id: String, val path: String)

data class WorldDefinition(val path: String) {
    fun toWorld(id: String) = World(id, path)
}

@JsonDeserialize(using = WorldReferenceDeserializer::class)
class WorldReference(id: String) : ResourceReference(id)

class WorldReferenceDeserializer : ResourceReferenceDeserializer<WorldReference>(
    WORLDS,
    ::WorldReference
)

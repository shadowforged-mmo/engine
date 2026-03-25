package com.shadowforgedmmo.engine.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.resource.ResourceReference
import com.shadowforgedmmo.engine.resource.ResourceReferenceDeserializer
import team.unnamed.hephaestus.Model as HephaestusModel

class BlockbenchModel(val id: String, val model: HephaestusModel)

class BlockbenchModelDefinition(val model: HephaestusModel) {
    fun toBlockbenchModel(id: String) = BlockbenchModel(id, model)
}

@JsonDeserialize(using = BlockbenchModelReferenceDeserializer::class)
class BlockbenchModelReference(id: String) : ResourceReference(id)

class BlockbenchModelReferenceDeserializer : ResourceReferenceDeserializer<BlockbenchModelReference>(
    "models",
    ::BlockbenchModelReference
)

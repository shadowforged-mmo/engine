package com.shadowforgedmmo.engine.minecraft

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.resource.MINECRAFT
import com.shadowforgedmmo.engine.resource.ResourceReference
import com.shadowforgedmmo.engine.resource.ResourceReferenceDeserializer

@JsonDeserialize(using = MinecraftReferenceDeserializer::class)
class MinecraftReference(id: String) : ResourceReference(id)

class MinecraftReferenceDeserializer : ResourceReferenceDeserializer<MinecraftReference>(
    MINECRAFT,
    ::MinecraftReference
)

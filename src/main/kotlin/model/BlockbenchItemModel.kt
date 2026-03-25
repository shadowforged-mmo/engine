package com.shadowforgedmmo.engine.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.resource.ITEM_MODELS
import com.shadowforgedmmo.engine.resource.ResourceReference
import com.shadowforgedmmo.engine.resource.ResourceReferenceDeserializer
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import team.unnamed.hephaestus.Model
import team.unnamed.hephaestus.reader.ModelReader
import java.io.File

class BlockbenchItemModel(val id: String, val customModelData: Int) {
    val itemStack = ItemStack.builder(Material.GLOW_ITEM_FRAME).build()
}

class BlockbenchItemModelDefinition(val model: Model) {
    fun toBlockbenchItemModel(id: String, customModelData: Int) = BlockbenchItemModel(id, customModelData)
    fun toBlockbenchItemModelAsset(id: String) = BlockbenchItemModelAsset(id, model)
}

@JsonDeserialize(using = BlockbenchItemModelReferenceDeserializer::class)
class BlockbenchItemModelReference(id: String) : ResourceReference(id)

class BlockbenchItemModelReferenceDeserializer : ResourceReferenceDeserializer<BlockbenchItemModelReference>(
    ITEM_MODELS,
    ::BlockbenchItemModelReference
)

class BlockbenchItemModelAsset(val id: String, val model: Model)

// TODO: remove
fun deserializeBlockbenchItemModelAsset(
    id: String,
    file: File,
    modelReader: ModelReader
) = BlockbenchItemModelAsset(id, modelReader.read(file))

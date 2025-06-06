package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.model.ArmorModel
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.model.deserializeArmorModel

class ArmorItem(
    id: String,
    name: String,
    quality: ItemQuality,
    val slot: ArmorSlot,
    val model: ArmorModel
) : Item(id, name, quality)

enum class ArmorSlot { FEET, LEGS, CHEST, HEAD }

fun deserializeArmorItem(
    id: String,
    data: JsonNode,
    blockbenchItemModelsById: Map<String, BlockbenchItemModel>
) = ArmorItem(
    id,
    data["name"].asText(),
    deserializeItemQuality(data["quality"]),
    deserializeArmorSlot(data["slot"]),
    deserializeArmorModel(data["model"], blockbenchItemModelsById)
)

fun deserializeArmorSlot(data: JsonNode) = ArmorSlot.valueOf(data.asText().uppercase())

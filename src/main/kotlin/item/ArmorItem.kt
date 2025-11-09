package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.model.ArmorModel
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.model.deserializeArmorModel
import com.shadowforgedmmo.engine.resource.deserializeEnum
import com.shadowforgedmmo.engine.runtime.Runtime
import net.minestom.server.item.ItemStack

class ArmorItem(
    id: String,
    name: String,
    quality: ItemQuality,
    val slot: ArmorSlot,
    sockets: Int,
    val model: ArmorModel
) : EquipmentItem(id, name, quality, sockets) {
    override fun instance(gems: List<Gem>) = ArmorItemInstance(this, gems)
}

enum class ArmorSlot { FEET, LEGS, CHEST, HEAD }

class ArmorItemInstance(item: ArmorItem, gems: List<Gem>) : EquipmentItemInstance(item, gems) {
    override val quantity
        get() = 1

    override fun itemStack(pc: PlayerCharacter) = ItemStack.builder(TODO())
        .build()
}

fun deserializeArmorItem(
    id: String,
    data: JsonNode,
    blockbenchItemModelsById: Map<String, BlockbenchItemModel>
) = ArmorItem(
    id,
    data["name"].asText(),
    deserializeEnum(data["quality"]),
    deserializeEnum(data["slot"]),
    data["sockets"]?.asInt() ?: 0,
    deserializeArmorModel(data["model"], blockbenchItemModelsById)
)

fun deserializeArmorItemInstance(data: JsonNode, runtime: Runtime) =
    deserializeItemInstance(data, runtime) as ArmorItemInstance

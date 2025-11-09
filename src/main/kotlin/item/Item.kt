package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.resource.parseId
import com.shadowforgedmmo.engine.runtime.Runtime
import net.minestom.server.item.ItemStack
import net.minestom.server.tag.Tag

val ITEM_ID_TAG = Tag.String("item_id")

fun socketTag(slot: Int) = Tag.String("socket_$slot")

abstract class Item(
    val id: String,
    val name: String,
    val quality: ItemQuality
)

abstract class ItemInstance(val item: Item) {
    abstract val quantity: Int

    abstract fun itemStack(pc: PlayerCharacter): ItemStack
}

fun deserializeItem(
    id: String,
    data: JsonNode,
    blockbenchItemModelsById: Map<String, BlockbenchItemModel>
) = when (data["type"].asText()) {
    "accessory" -> deserializeAccessory(id, data)
    "armor" -> deserializeArmorItem(id, data, blockbenchItemModelsById)
    "consumable" -> deserializeConsumableItem(id, data)
    "quest" -> deserializeQuestItem(id, data)
    "weapon" -> deserializeWeapon(id, data, blockbenchItemModelsById)
    else -> throw IllegalArgumentException()
}

fun deserializeItemInstance(data: JsonNode, runtime: Runtime): ItemInstance {
    val id = parseItemId(data["id"].asText())
    return when (val item = runtime.itemsById.getValue(id)) {
        is EquipmentItem -> item.instance(
            data["gems"].map(JsonNode::asText).map(runtime.itemsById::getValue).map { it as Gem }
        )

        is ConsumableItem -> ConsumableItemInstance(item, data["quantity"].asInt())
        is QuestItem -> QuestItemInstance(item, data["quantity"].asInt()) // TODO: need this?
        else -> throw IllegalArgumentException()
    }
}

fun parseItemId(id: String) = parseId(id, "items")

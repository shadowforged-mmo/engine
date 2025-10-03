package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.resource.deserializeEnum
import net.minestom.server.item.ItemStack

class ConsumableItem(
    id: String,
    name: String,
    quality: ItemQuality
) : Item(id, name, quality)

class ConsumableItemInstance(
    item: ConsumableItem,
    override val quantity: Int
) : ItemInstance(item) {
    override fun itemStack(pc: PlayerCharacter) = ItemStack.builder(TODO()).build()
}

fun deserializeConsumableItem(id: String, data: JsonNode) = ConsumableItem(
    id,
    data["name"].asText(),
    deserializeEnum(data["quality"])
)

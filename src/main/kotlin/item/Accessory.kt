package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.resource.deserializeEnum
import com.shadowforgedmmo.engine.runtime.Runtime
import net.minestom.server.item.ItemStack

class Accessory(
    id: String,
    name: String,
    quality: ItemQuality,
    val slot: AccessorySlot,
    sockets: Int
) : EquipmentItem(id, name, quality, sockets) {
    override fun instance(gems: List<Gem>) = AccessoryInstance(this, gems)
}

enum class AccessorySlot { FINGER_1, FINGER_2, WRIST, TRINKET }

class AccessoryInstance(item: Accessory, gems: List<Gem>) : EquipmentItemInstance(item, gems) {
    override val quantity
        get() = 1

    override fun itemStack(pc: PlayerCharacter) = ItemStack.builder(TODO())
        .build()
}

fun deserializeAccessory(id: String, data: JsonNode) = Accessory(
    id,
    data["name"].asText(),
    deserializeEnum(data["quality"]),
    deserializeEnum(data["slot"]),
    data["sockets"]?.asInt() ?: 0
)

fun deserializeAccessoryInstance(data: JsonNode, runtime: Runtime) =
    deserializeItemInstance(data, runtime) as AccessoryInstance

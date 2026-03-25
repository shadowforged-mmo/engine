package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.resource.EnumDeserializer
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

data class AccessoryDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("quality") val quality: ItemQuality,
    @JsonProperty("slot") val slot: AccessorySlot,
    @JsonProperty("sockets") val sockets: Int
) {
    fun toAccessory(id: String) = Accessory(id, name, quality, slot, sockets)
}

class AccessoryInstance(item: Accessory, gems: List<Gem>) : EquipmentItemInstance(item, gems) {
    override val quantity
        get() = 1

    override fun itemStack(pc: PlayerCharacter) = ItemStack.builder(TODO())
        .build()
}

@JsonDeserialize(using = EnumDeserializer::class)
enum class AccessorySlot { FINGER_1, FINGER_2, WRIST, TRINKET }

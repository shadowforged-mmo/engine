package com.shadowforgedmmo.engine.item

import com.shadowforgedmmo.engine.character.PlayerCharacter
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

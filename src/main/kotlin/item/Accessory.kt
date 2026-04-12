package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.icon.Icon
import com.shadowforgedmmo.engine.icon.IconReference
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.resource.Registry
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import kotlin.let

class Accessory(
    id: String,
    name: String,
    quality: ItemQuality,
    val icon: Icon,
    val slot: AccessorySlot,
    sockets: Int
) : EquipmentItem(id, name, quality, sockets) {
    override fun instance(gems: List<Gem>) = AccessoryInstance(this, gems)
}

data class AccessoryDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("quality") val quality: ItemQuality,
    @JsonProperty("icon") val iconReference: IconReference,
    @JsonProperty("slot") val slot: AccessorySlot,
    @JsonProperty("sockets") val sockets: Int
) : ItemDefinition() {
    override fun toItem(
        id: String,
        iconRegistry: Registry<Icon>,
        blockbenchItemModelRegistry: Registry<BlockbenchItemModel>
    ) = Accessory(
        id,
        name,
        quality,
        iconReference.resolve(iconRegistry),
        slot,
        sockets
    )
}

class AccessoryInstance(
    override val item: Accessory,
    gems: List<Gem>
) : EquipmentItemInstance(gems) {
    override val quantity
        get() = 1

    override fun itemStack(pc: PlayerCharacter) = ItemStack.builder(Material.DIAMOND)
        .let(item.icon::apply)
        .build()
}

enum class AccessorySlot { FINGER_1, FINGER_2, WRIST, TRINKET }

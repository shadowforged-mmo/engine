package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.icon.Icon
import com.shadowforgedmmo.engine.model.ArmorModel
import com.shadowforgedmmo.engine.model.ArmorModelDefinition
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.resource.Registry
import net.minestom.server.item.ItemStack

class ArmorItem(
    id: String,
    name: String,
    quality: ItemQuality,
    val slot: ArmorSlot,
    sockets: Int,
    val model: ArmorModel
) : EquipmentItem(id, name, quality, sockets) {
    override fun instance(socketables: List<Socketable>) = ArmorItemInstance(this, socketables)
}

data class ArmorItemDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("quality") val quality: ItemQuality,
    @JsonProperty("slot") val slot: ArmorSlot,
    @JsonProperty("sockets") val sockets: Int,
    @JsonProperty("model") val modelDefinition: ArmorModelDefinition
) : ItemDefinition() {
    override fun toItem(
        id: String,
        iconRegistry: Registry<Icon>,
        blockbenchItemModelRegistry: Registry<BlockbenchItemModel>
    ) = ArmorItem(
        id,
        name,
        quality,
        slot,
        sockets,
        modelDefinition.toArmorModel(blockbenchItemModelRegistry)
    )
}

class ArmorItemInstance(override val item: ArmorItem, socketables: List<Socketable>) : EquipmentItemInstance(socketables) {
    override val quantity
        get() = 1

    override fun itemStack(pc: PlayerCharacter) = ItemStack.builder(TODO())
        .build()
}

enum class ArmorSlot { FEET, LEGS, CHEST, HEAD }

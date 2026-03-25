package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.resource.Registry
import net.minestom.server.item.ItemStack

class ConsumableItem(
    id: String,
    name: String,
    quality: ItemQuality
) : Item(id, name, quality)

data class ConsumableItemDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("quality") val quality: ItemQuality
) : ItemDefinition() {
    override fun toItem(
        id: String,
        blockbenchItemModelRegistry: Registry<BlockbenchItemModel>
    ) = ConsumableItem(id, name, quality)
}

class ConsumableItemInstance(
    item: ConsumableItem,
    override val quantity: Int
) : ItemInstance(item) {
    override fun itemStack(pc: PlayerCharacter) = ItemStack.builder(TODO()).build()
}

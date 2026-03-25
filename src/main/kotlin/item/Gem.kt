package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.resource.Registry
import net.minestom.server.item.ItemStack

class Gem(
    id: String,
    name: String,
    quality: ItemQuality
) : Item(id, name, quality)

class GemDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("quality") val quality: ItemQuality
) : ItemDefinition() {
    override fun toItem(
        id: String,
        blockbenchItemModelRegistry: Registry<BlockbenchItemModel>
    ) = Gem(id, name, quality)
}

class GemInstance(item: Gem, override val quantity: Int) : ItemInstance(item) {
    override fun itemStack(pc: PlayerCharacter) = ItemStack.builder(TODO())
        .build()
}

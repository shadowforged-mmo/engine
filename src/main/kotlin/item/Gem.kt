package com.shadowforgedmmo.engine.item

import com.shadowforgedmmo.engine.character.PlayerCharacter
import net.minestom.server.item.ItemStack

class Gem(
    id: String,
    name: String,
    quality: ItemQuality
) : Item(id, name, quality)

class GemInstance(item: Gem, override val quantity: Int) : ItemInstance(item) {
    override fun itemStack(pc: PlayerCharacter) = ItemStack.builder(TODO())
        .build()
}

package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.icon.Icon
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.resource.Registry
import net.minestom.server.item.ItemStack

class Socketable(
    id: String,
    name: String,
    quality: ItemQuality,
    val bonuses: Bonuses
) : Item(id, name, quality)

class SocketableDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("quality") val quality: ItemQuality,
    @JsonProperty("bonuses") val bonuses: Bonuses?
) : ItemDefinition() {
    override fun toItem(
        id: String,
        iconRegistry: Registry<Icon>,
        blockbenchItemModelRegistry: Registry<BlockbenchItemModel>
    ) = Socketable(id, name, quality, bonuses ?: Bonuses())
}

class SocketableInstance(override val item: Socketable, override val quantity: Int) : ItemInstance() {
    override fun itemStack(pc: PlayerCharacter) = ItemStack.builder(TODO())
        .build()
}

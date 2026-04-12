package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.icon.Icon
import com.shadowforgedmmo.engine.icon.IconReference
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.resource.Registry
import com.shadowforgedmmo.engine.script.ScriptReference
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class ConsumableItem(
    id: String,
    name: String,
    quality: ItemQuality,
    val level: Int,
    val icon: Icon
) : Item(id, name, quality)

data class ConsumableItemDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("quality") val quality: ItemQuality,
    @JsonProperty("level") val level: Int,
    @JsonProperty("icon") val iconReference: IconReference,
    @JsonProperty("script") val scriptReference: ScriptReference
) : ItemDefinition() {
    override fun toItem(
        id: String,
        iconRegistry: Registry<Icon>,
        blockbenchItemModelRegistry: Registry<BlockbenchItemModel>
    ) = ConsumableItem(
        id,
        name,
        quality,
        level,
        iconReference.resolve(iconRegistry)
    )
}

class ConsumableItemInstance(
    override val item: ConsumableItem,
    override val quantity: Int
) : ItemInstance() {
    override fun itemStack(pc: PlayerCharacter) = ItemStack.builder(Material.DIAMOND)
        .customName(item.nameComponent)
        .set(ITEM_ID_TAG, item.id)
        .let(item.icon::apply)
        .build()
}

package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.icon.Icon
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.resource.Registry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class QuestItem(
    id: String,
    name: String,
    quality: ItemQuality
) : Item(id, name, quality)

class QuestItemDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("quality") val quality: ItemQuality
) : ItemDefinition() {
    override fun toItem(
        id: String,
        iconRegistry: Registry<Icon>,
        blockbenchItemModelRegistry: Registry<BlockbenchItemModel>
    ) = QuestItem(id, name, quality)
}

class QuestItemInstance(override val item: QuestItem, override val quantity: Int) : ItemInstance() {
    override fun itemStack(pc: PlayerCharacter) = ItemStack.builder(Material.DIAMOND)
        .set(ITEM_ID_TAG, item.id)
        .customName(
            Component.text(item.name, item.quality.color).decoration(TextDecoration.ITALIC, false)
        )
        .build()
}

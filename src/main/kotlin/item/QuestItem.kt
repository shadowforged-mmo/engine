package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.icon.Icon
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.resource.Registry
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class QuestItem(
    id: String,
    name: String,
    quality: ItemQuality,
    level: Int,
    flavorText: String?,
    sellPrice: Int?
) : Item(id, name, quality, level, flavorText, sellPrice)

class QuestItemDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("quality") val quality: ItemQuality,
    @JsonProperty("level") val level: Int,
    @JsonProperty("flavor_text") val flavorText: String? = null,
    @JsonProperty("sell_price") val sellPrice: Int? = null
) : ItemDefinition() {
    override fun toItem(
        id: String,
        iconRegistry: Registry<Icon>,
        blockbenchItemModelRegistry: Registry<BlockbenchItemModel>
    ) = QuestItem(id, name, quality, level, flavorText, sellPrice)
}

class QuestItemInstance(override val item: QuestItem, override val quantity: Int) : ItemInstance() {
    override fun itemStack(pc: PlayerCharacter) = ItemStack.builder(Material.DIAMOND)
        .set(ITEM_ID_TAG, item.id)
        .customName(item.nameComponent.noItalic())
        .lore(listOfNotNull(
            levelLine(item.level),
            flavorLine(item.flavorText),
            sellPriceLine(item.sellPrice),
        ))
        .build()
}

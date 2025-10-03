package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.resource.deserializeEnum
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class QuestItem(
    id: String,
    name: String,
    quality: ItemQuality
) : Item(id, name, quality)

class QuestItemInstance(item: QuestItem, override val quantity: Int) : ItemInstance(item) {
    override fun itemStack(pc: PlayerCharacter) = ItemStack.builder(Material.DIAMOND)
        .set(ITEM_ID_TAG, item.id)
        .customName(
            Component.text(item.name, item.quality.color).decoration(TextDecoration.ITALIC, false)
        )
        .build()
}

fun deserializeQuestItem(id: String, data: JsonNode) = QuestItem(
    id,
    data["name"].asText(),
    deserializeEnum(data["quality"])
)

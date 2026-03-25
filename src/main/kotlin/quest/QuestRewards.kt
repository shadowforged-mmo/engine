package com.shadowforgedmmo.engine.quest

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.item.Item
import com.shadowforgedmmo.engine.item.ItemReference
import com.shadowforgedmmo.engine.playerclass.PlayerClass
import com.shadowforgedmmo.engine.playerclass.PlayerClassReference
import com.shadowforgedmmo.engine.resource.Registry

class QuestRewards(
    val experience: Int,
    val currency: Int,
    val items: List<QuestRewardItem>
)

data class QuestRewardsDefinition(
    @JsonProperty("experience") val experience: Int?,
    @JsonProperty("currency") val currency: Int?,
    @JsonProperty("items") val items: List<QuestRewardItemDefinition>?
) {
    fun toQuestRewards(
        itemRegistry: Registry<Item>,
        playerClassRegistry: Registry<PlayerClass>
    ) = QuestRewards(
        experience ?: 0,
        currency ?: 0,
        items?.map { it.toQuestRewardItem(itemRegistry, playerClassRegistry) } ?: emptyList()
    )
}

class QuestRewardItem(
    val item: Item,
    val quantity: Int,
    val playerClasses: Collection<PlayerClass>?
)

data class QuestRewardItemDefinition(
    @JsonProperty("item") val itemReference: ItemReference,
    @JsonProperty("quantity") val quantity: Int?,
    @JsonProperty("player_classes") val playerClassReferences: List<PlayerClassReference>?
) {
    fun toQuestRewardItem(
        itemRegistry: Registry<Item>,
        playerClassRegistry: Registry<PlayerClass>
    ) = QuestRewardItem(
        itemReference.resolve(itemRegistry),
        quantity ?: 1,
        playerClassReferences?.map { it.resolve(playerClassRegistry) }
    )
}

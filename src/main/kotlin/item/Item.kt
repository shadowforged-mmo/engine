package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.shadowforgedmmo.engine.behavior.PrioritySelectorDefinition
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.resource.Registry
import com.shadowforgedmmo.engine.resource.ResourceReference
import net.minestom.server.item.ItemStack
import net.minestom.server.tag.Tag

val ITEM_ID_TAG = Tag.String("item_id")

fun socketTag(slot: Int) = Tag.String("socket_$slot")

abstract class Item(
    val id: String,
    val name: String,
    val quality: ItemQuality
)

abstract class ItemInstance(val item: Item) {
    abstract val quantity: Int

    abstract fun itemStack(pc: PlayerCharacter): ItemStack
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = PrioritySelectorDefinition::class, name = "accessory"),
    JsonSubTypes.Type(value = ArmorItemDefinition::class, name = "armor"),
    JsonSubTypes.Type(value = ConsumableItemDefinition::class, name = "consumable"),
    JsonSubTypes.Type(value = GemDefinition::class, name = "gem"),
    JsonSubTypes.Type(value = QuestItemDefinition::class, name = "quest"),
    JsonSubTypes.Type(value = WeaponDefinition::class, name = "weapon")
)
sealed class ItemDefinition {
    abstract fun toItem(
        id: String,
        blockbenchItemModelRegistry: Registry<BlockbenchItemModel>
    ): Item
}

// TODO: handle nulls correctly
class ItemInstanceDefinition(
    @JsonProperty("item") val itemReference: ItemReference,
    @JsonProperty("quantity") val quantity: Int?,
    @JsonProperty("gems") val gems: List<ItemReference>?
) {
    fun toItemInstance(itemsById: Map<String, Item>) = when (val item = itemReference.resolve(itemsById)) {
        is EquipmentItem -> item.instance(
            gems?.map { it.resolve(itemsById) as? Gem ?: error("${it.id} is not a gem") } ?: emptyList()
        )

        is ConsumableItem -> ConsumableItemInstance(item, quantity ?: 1)
        is QuestItem -> QuestItemInstance(item, quantity ?: 1) // TODO: need this?
        else -> throw IllegalArgumentException()
    }
}

class ItemReference(id: String) : ResourceReference(id)

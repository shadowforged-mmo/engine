package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.behavior.PrioritySelectorDefinition
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.icon.Icon
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.resource.ITEMS
import com.shadowforgedmmo.engine.resource.Registry
import com.shadowforgedmmo.engine.resource.ResourceReference
import com.shadowforgedmmo.engine.resource.ResourceReferenceDeserializer
import net.kyori.adventure.text.Component
import net.minestom.server.item.ItemStack
import net.minestom.server.tag.Tag

val ITEM_ID_TAG = Tag.String("item_id")

fun socketTag(slot: Int) = Tag.String("socket_$slot")

abstract class Item(
    val id: String,
    val name: String,
    val quality: ItemQuality
) {
    val nameComponent
        get() = Component.text(name, quality.color)
}

abstract class ItemInstance {
    companion object {
        fun fromItemStack(itemStack: ItemStack, itemRegistry: Registry<Item>) =
            when (val item = itemRegistry[itemStack.getTag(ITEM_ID_TAG)]) {
                is EquipmentItem -> item.instance(getSocketables(itemStack, itemRegistry))
                is ConsumableItem -> ConsumableItemInstance(item, itemStack.amount())
                is Socketable -> SocketableInstance(item, itemStack.amount())
                is QuestItem -> QuestItemInstance(item, itemStack.amount())
                else -> null
            }

        fun getSocketables(itemStack: ItemStack, itemRegistry: Registry<Item>) =
            generateSequence(0) { it + 1 }
                .takeWhile { itemStack.hasTag(socketableTag(it)) }
                .map { itemStack.getTag(socketableTag(it)) }
                .mapNotNull { socketableId -> itemRegistry[socketableId] as? Socketable }
                .toList()

        fun socketableTag(slot: Int) = Tag.String("socketable_$slot")
    }

    abstract val item: Item
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
    JsonSubTypes.Type(value = SocketableDefinition::class, name = "socketable"),
    JsonSubTypes.Type(value = QuestItemDefinition::class, name = "quest"),
    JsonSubTypes.Type(value = WeaponDefinition::class, name = "weapon")
)
sealed class ItemDefinition {
    abstract fun toItem(
        id: String,
        iconRegistry: Registry<Icon>,
        blockbenchItemModelRegistry: Registry<BlockbenchItemModel>
    ): Item
}

class ItemInstanceDefinition(
    @JsonProperty("item") val itemReference: ItemReference,
    @JsonProperty("quantity") val quantity: Int?,
    @JsonProperty("socketables") val socketables: List<ItemReference>?
) {
    fun toItemInstance(itemRegistry: Registry<Item>) = when (val item = itemReference.resolve(itemRegistry)) {
        is EquipmentItem -> item.instance(
            socketables?.map { it.resolve(itemRegistry) as? Socketable ?: error("${it.id} is not a socketable") } ?: emptyList()
        )

        is ConsumableItem -> ConsumableItemInstance(item, quantity ?: 1)
        is QuestItem -> QuestItemInstance(item, quantity ?: 1) // TODO: need this?
        else -> throw IllegalArgumentException()
    }
}

@JsonDeserialize(using = ItemReferenceDeserializer::class)
class ItemReference(id: String) : ResourceReference(id)

class ItemReferenceDeserializer : ResourceReferenceDeserializer<ItemReference>(
    ITEMS,
    ::ItemReference
)

package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.model.BlockbenchItemModelReference
import com.shadowforgedmmo.engine.resource.EnumDeserializer
import com.shadowforgedmmo.engine.resource.Registry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class Weapon(
    id: String,
    name: String,
    quality: ItemQuality,
    val type: WeaponType,
    sockets: Int,
    val model: BlockbenchItemModel
) : EquipmentItem(id, name, quality, sockets) {
    override fun instance(gems: List<Gem>) = WeaponInstance(this, gems)
}

data class WeaponDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("quality") val quality: ItemQuality,
    @JsonProperty("type") val type: WeaponType,
    @JsonProperty("sockets") val sockets: Int,
    @JsonProperty("model") val modelReference: BlockbenchItemModelReference
) : ItemDefinition() {
    override fun toItem(
        id: String,
        blockbenchItemModelRegistry: Registry<BlockbenchItemModel>
    ) = Weapon(id, name, quality, type, sockets, modelReference.resolve(blockbenchItemModelRegistry))
}

class WeaponInstance(item: Weapon, gems: List<Gem>) : EquipmentItemInstance(item, gems) {
    override fun itemStack(pc: PlayerCharacter) = ItemStack.builder(Material.IRON_SWORD)
        .set(ITEM_ID_TAG, item.id)
        .customName(
            Component.text(item.name, item.quality.color).decoration(TextDecoration.ITALIC, false)
        )
        .build()
}

@JsonDeserialize(using = EnumDeserializer::class)
enum class WeaponType {
    SWORD
}

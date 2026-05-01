package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.icon.Icon
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.model.BlockbenchItemModelReference
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
    val model: BlockbenchItemModel,
    val bonuses: Bonuses,
    val flavorText: String?,
    val sellPrice: Int?
) : EquipmentItem(id, name, quality, sockets) {
    override fun instance(socketables: List<Socketable>) = WeaponInstance(this, socketables)
}

data class WeaponDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("quality") val quality: ItemQuality,
    @JsonProperty("level") val level: Int,
    @JsonProperty("weapon_type") val type: WeaponType,
    @JsonProperty("sockets") val sockets: Int,
    @JsonProperty("model") val modelReference: BlockbenchItemModelReference,
    @JsonProperty("attack_speed") val attackSpeed: Double,
    @JsonProperty("physical_damage") val physicalDamage: Double,
    @JsonProperty("water_damage") val waterDamage: Double,
    @JsonProperty("lightning_damage") val lightningDamage: Double,
    @JsonProperty("bonuses") val bonuses: Bonuses = Bonuses(),
    @JsonProperty("flavor_text") val flavorText: String? = null,
    @JsonProperty("sell_price") val sellPrice: Int? = null
) : ItemDefinition() {
    override fun toItem(
        id: String,
        iconRegistry: Registry<Icon>,
        blockbenchItemModelRegistry: Registry<BlockbenchItemModel>
    ) = Weapon(
        id,
        name,
        quality,
        type,
        sockets,
        modelReference.resolve(blockbenchItemModelRegistry),
        bonuses,
        flavorText,
        sellPrice
    )
}

class WeaponInstance(override val item: Weapon, socketables: List<Socketable>) : EquipmentItemInstance(socketables) {
    override fun itemStack(pc: PlayerCharacter) = ItemStack.builder(Material.WOODEN_AXE)
        .set(ITEM_ID_TAG, item.id)
        .let(item.model::apply)
        .customName(
            Component.text(item.name, item.quality.color).decoration(TextDecoration.ITALIC, false)
        )
        .build()
}

enum class WeaponType {
    SWORD
}

package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.icon.Icon
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.model.BlockbenchItemModelReference
import com.shadowforgedmmo.engine.resource.Registry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class Weapon(
    id: String,
    name: String,
    quality: ItemQuality,
    level: Int,
    flavorText: String?,
    sellPrice: Int?,
    requiredLevel: Int?,
    val type: WeaponType,
    val model: BlockbenchItemModel,
    val attackSpeed: Double,
    val damage: WeaponDamage,
    sockets: Int,
    bonuses: Bonuses,
) : EquipmentItem(id, name, quality, level, flavorText, sellPrice, sockets, bonuses, requiredLevel) {
    override fun instance(socketables: Array<Socketable?>) = WeaponInstance(this, socketables)
}

data class WeaponDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("quality") val quality: ItemQuality,
    @JsonProperty("level") val level: Int,
    @JsonProperty("required_level") val requiredLevel: Int,
    @JsonProperty("weapon_type") val type: WeaponType,
    @JsonProperty("model") val modelReference: BlockbenchItemModelReference,
    @JsonProperty("attack_speed") val attackSpeed: Double,
    @JsonProperty("damage") val damage: WeaponDamage? = null,
    @JsonProperty("sockets") val sockets: Int?,
    @JsonProperty("bonuses") val bonuses: BonusesDefinition?,
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
        level,
        flavorText,
        sellPrice,
        requiredLevel,
        type,
        modelReference.resolve(blockbenchItemModelRegistry),
        attackSpeed,
        damage ?: WeaponDamage(),
        sockets ?: 0,
        bonuses?.toBonuses() ?: Bonuses(),
    )
}

class WeaponInstance(override val item: Weapon, socketables: Array<Socketable?>) : EquipmentItemInstance(socketables) {
    override fun baseBuilder() = ItemStack.builder(Material.WOODEN_AXE).let(item.model::apply)

    override fun typeComponent() = Component.text(item.type.text, NamedTextColor.GRAY)

    override fun preBonusLore() =
        listOf(loreLine("${item.attackSpeed} Attack Speed", NamedTextColor.WHITE)) +
                item.damage.components()
}

enum class WeaponType(val text: String) {
    ONE_HANDED_AXE("Axe"),
    DAGGER("Dagger"),
    ONE_HANDED_MACE("Mace"),
    POLEARM("Polearm"),
    STAFF("Staff"),
    SWORD("Sword"),
    WAND("Wand")
}

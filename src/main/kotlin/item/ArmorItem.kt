package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.icon.Icon
import com.shadowforgedmmo.engine.model.ArmorModel
import com.shadowforgedmmo.engine.model.ArmorModelDefinition
import com.shadowforgedmmo.engine.model.ArmorPiece
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.resource.Registry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class ArmorItem(
    id: String,
    name: String,
    quality: ItemQuality,
    level: Int,
    flavorText: String?,
    sellPrice: Int?,
    val slot: ArmorSlot,
    sockets: Int,
    val model: ArmorModel,
    val armor: Armor,
    bonuses: Bonuses,
    requiredLevel: Int?
) : EquipmentItem(id, name, quality, level, flavorText, sellPrice, sockets, bonuses, requiredLevel) {
    override fun instance(socketables: Array<Socketable?>) = ArmorItemInstance(this, socketables)
}

data class ArmorItemDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("quality") val quality: ItemQuality,
    @JsonProperty("level") val level: Int,
    @JsonProperty("slot") val slot: ArmorSlot,
    @JsonProperty("sockets") val sockets: Int,
    @JsonProperty("model") val modelDefinition: ArmorModelDefinition,
    @JsonProperty("armor") val armor: Armor? = null,
    @JsonProperty("bonuses") val bonuses: BonusesDefinition?,
    @JsonProperty("required_level") val requiredLevel: Int? = null,
    @JsonProperty("flavor_text") val flavorText: String? = null,
    @JsonProperty("sell_price") val sellPrice: Int? = null
) : ItemDefinition() {
    override fun toItem(
        id: String,
        iconRegistry: Registry<Icon>,
        blockbenchItemModelRegistry: Registry<BlockbenchItemModel>
    ) = ArmorItem(
        id,
        name,
        quality,
        level,
        flavorText,
        sellPrice,
        slot,
        sockets,
        modelDefinition.toArmorModel(blockbenchItemModelRegistry),
        armor ?: Armor(),
        bonuses?.toBonuses() ?: Bonuses(),
        requiredLevel
    )
}

class ArmorItemInstance(
    override val item: ArmorItem,
    socketables: Array<Socketable?>
) : EquipmentItemInstance(socketables) {
    override fun baseBuilder() = item.model.baseBuilder(item.slot.piece)

    override fun typeComponent() = Component.text(item.slot.text, NamedTextColor.GRAY)

    override fun preBonusLore() = item.armor.components()
}

enum class ArmorSlot(val text: String, val piece: ArmorPiece) {
    FEET("Feet", ArmorPiece.BOOTS),
    LEGS("Legs", ArmorPiece.LEGGINGS),
    CHEST("Chest", ArmorPiece.CHESTPLATE),
    HEAD("Head", ArmorPiece.HELMET)
}

package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.icon.Icon
import com.shadowforgedmmo.engine.icon.IconReference
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.resource.Registry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class Accessory(
    id: String,
    name: String,
    quality: ItemQuality,
    level: Int,
    flavorText: String?,
    sellPrice: Int?,
    val icon: Icon,
    val slot: AccessorySlot,
    sockets: Int,
    bonuses: Bonuses,
    requiredLevel: Int?
) : EquipmentItem(id, name, quality, level, flavorText, sellPrice, sockets, bonuses, requiredLevel) {
    override fun instance(socketables: Array<Socketable?>) = AccessoryInstance(this, socketables)
}

data class AccessoryDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("quality") val quality: ItemQuality,
    @JsonProperty("level") val level: Int,
    @JsonProperty("icon") val iconReference: IconReference,
    @JsonProperty("slot") val slot: AccessorySlot,
    @JsonProperty("sockets") val sockets: Int,
    @JsonProperty("bonuses") val bonuses: BonusesDefinition?,
    @JsonProperty("required_level") val requiredLevel: Int? = null,
    @JsonProperty("flavor_text") val flavorText: String? = null,
    @JsonProperty("sell_price") val sellPrice: Int? = null
) : ItemDefinition() {
    override fun toItem(
        id: String,
        iconRegistry: Registry<Icon>,
        blockbenchItemModelRegistry: Registry<BlockbenchItemModel>
    ) = Accessory(
        id,
        name,
        quality,
        level,
        flavorText,
        sellPrice,
        iconReference.resolve(iconRegistry),
        slot,
        sockets,
        bonuses?.toBonuses() ?: Bonuses(),
        requiredLevel
    )
}

class AccessoryInstance(
    override val item: Accessory,
    socketables: Array<Socketable?>
) : EquipmentItemInstance(socketables) {
    override fun baseBuilder() = ItemStack.builder(Material.DIAMOND).let(item.icon::apply)

    override fun typeComponent() = Component.text(item.slot.text, NamedTextColor.GRAY)
}

enum class AccessorySlot(val text: String) {
    FINGER("Finger"),
    WRIST("Wrist"),
    TRINKET("Trinket")
}

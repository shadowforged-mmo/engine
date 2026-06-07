package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.icon.Icon
import com.shadowforgedmmo.engine.icon.IconReference
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.resource.Registry
import com.shadowforgedmmo.engine.script.Script
import com.shadowforgedmmo.engine.script.ScriptReference
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class ConsumableItem(
    id: String,
    name: String,
    quality: ItemQuality,
    level: Int,
    flavorText: String?,
    sellPrice: Int?,
    val icon: Icon,
    val use: String,
    val requiredLevel: Int?,
    val script: Script
) : Item(id, name, quality, level, flavorText, sellPrice)

data class ConsumableItemDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("quality") val quality: ItemQuality,
    @JsonProperty("level") val level: Int,
    @JsonProperty("icon") val iconReference: IconReference,
    @JsonProperty("script") val scriptReference: ScriptReference,
    @JsonProperty("use") val use: String,
    @JsonProperty("sell_price") val sellPrice: Int? = null,
    @JsonProperty("required_level") val requiredLevel: Int? = null,
    @JsonProperty("flavor_text") val flavorText: String? = null
) : ItemDefinition() {
    override fun toItem(
        id: String,
        iconRegistry: Registry<Icon>,
        blockbenchItemModelRegistry: Registry<BlockbenchItemModel>
    ) = ConsumableItem(
        id,
        name,
        quality,
        level,
        flavorText,
        sellPrice,
        iconReference.resolve(iconRegistry),
        use,
        requiredLevel,
        Script(scriptReference.id)
    )
}

class ConsumableItemInstance(
    override val item: ConsumableItem,
    override val quantity: Int
) : ItemInstance() {
    override fun itemStack(pc: PlayerCharacter) = ItemStack.builder(Material.DIAMOND)
        .customName(item.nameComponent.noItalic())
        .lore(listOfNotNull(
            levelLine(item.level),
            requiredLevelLine(item.requiredLevel),
            useLine(item.use),
            flavorLine(item.flavorText),
            sellPriceLine(item.sellPrice),
        ))
        .set(ITEM_ID_TAG, item.id)
        .let(item.icon::apply)
        .amount(quantity)
        .build()
}

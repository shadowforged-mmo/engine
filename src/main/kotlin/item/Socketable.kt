package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.icon.Icon
import com.shadowforgedmmo.engine.icon.IconReference
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.resource.Registry
import net.minestom.server.component.DataComponents
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.component.TooltipDisplay

class Socketable(
    id: String,
    name: String,
    quality: ItemQuality,
    level: Int,
    flavorText: String?,
    sellPrice: Int?,
    val icon: Icon,
    val bonuses: Bonuses,
) : Item(id, name, quality, level, flavorText, sellPrice)

class SocketableDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("quality") val quality: ItemQuality,
    @JsonProperty("level") val level: Int,
    @JsonProperty("icon") val iconReference: IconReference,
    @JsonProperty("bonuses") val bonuses: BonusesDefinition?,
    @JsonProperty("flavor_text") val flavorText: String? = null,
    @JsonProperty("sell_price") val sellPrice: Int? = null
) : ItemDefinition() {
    override fun toItem(
        id: String,
        iconRegistry: Registry<Icon>,
        blockbenchItemModelRegistry: Registry<BlockbenchItemModel>
    ) = Socketable(
        id,
        name,
        quality,
        level,
        flavorText,
        sellPrice,
        iconReference.resolve(iconRegistry),
        bonuses?.toBonuses() ?: Bonuses(),
    )
}

class SocketableInstance(override val item: Socketable, override val quantity: Int) : ItemInstance() {
    override fun itemStack(pc: PlayerCharacter) = ItemStack.builder(Material.AMETHYST_SHARD)
        .set(ITEM_ID_TAG, item.id)
        .customName(item.nameComponent.noItalic())
        .lore(
            listOf(levelLine(item.level)) +
                    item.bonuses.components() +
                    listOfNotNull(
                        flavorLine(item.flavorText),
                        sellPriceLine(item.sellPrice),
                    )
        )
        .set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay(false, setOf(DataComponents.ATTRIBUTE_MODIFIERS)))
        .amount(quantity)
        .build()
}

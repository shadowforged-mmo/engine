package com.shadowforgedmmo.engine.item

import com.shadowforgedmmo.engine.character.PlayerCharacter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.component.DataComponents
import net.minestom.server.item.ItemStack
import net.minestom.server.item.component.TooltipDisplay

abstract class EquipmentItem(
    id: String,
    name: String,
    quality: ItemQuality,
    level: Int,
    flavorText: String?,
    sellPrice: Int?,
    val sockets: Int,
    val bonuses: Bonuses,
    val requiredLevel: Int?
) : Item(id, name, quality, level, flavorText, sellPrice) {
    abstract fun instance(socketables: Array<Socketable?>): EquipmentItemInstance
}

abstract class EquipmentItemInstance(val socketables: Array<Socketable?>) : ItemInstance() {
    abstract override val item: EquipmentItem
    override val quantity: Int get() = 1

    protected abstract fun baseBuilder(): ItemStack.Builder
    protected open fun typeComponent(): Component? = null
    protected open fun preBonusLore(): List<Component> = emptyList()

    private fun socketLore(): List<Component> =
        socketables.flatMap { socketable ->
            if (socketable != null)
                listOf(
                    Component.text("⦿ ", NamedTextColor.GRAY).append(socketable.nameComponent).noItalic()
                ) +
                        socketable.bonuses.components().map {
                            Component.text("  ").append(it.color(NamedTextColor.YELLOW)).noItalic()
                        }
            else
                listOf(loreLine("⦾ Empty Socket", NamedTextColor.GRAY))
        }

    override fun itemStack(pc: PlayerCharacter): ItemStack {
        val lore = listOfNotNull(levelLine(item.level), typeComponent()?.noItalic()) +
                preBonusLore() +
                item.bonuses.components() +
                socketLore() +
                listOfNotNull(
                    requiredLevelLine(item.requiredLevel),
                    flavorLine(item.flavorText),
                    sellPriceLine(item.sellPrice),
                )

        return baseBuilder()
            .set(ITEM_ID_TAG, item.id)
            .customName(item.nameComponent.noItalic())
            .lore(lore)
            .set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay(false, setOf(DataComponents.ATTRIBUTE_MODIFIERS)))
            .build()
    }
}

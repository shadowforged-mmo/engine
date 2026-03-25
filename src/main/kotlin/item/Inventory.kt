package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.resource.Registry
import net.minestom.server.inventory.PlayerInventory
import net.minestom.server.utils.inventory.PlayerInventoryUtils

class Inventory(val inventory: PlayerInventory, val itemRegistry: Registry<Item>) {
    val weapon: WeaponInstance?
        get() = equipment(8) as? WeaponInstance

    val offhand: EquipmentItemInstance?
        get() = equipment(PlayerInventoryUtils.OFFHAND_SLOT)

    val feet: ArmorItemInstance?
        get() = equipment(PlayerInventoryUtils.BOOTS_SLOT) as? ArmorItemInstance

    val legs: ArmorItemInstance?
        get() = equipment(PlayerInventoryUtils.LEGGINGS_SLOT) as? ArmorItemInstance

    val chest: ArmorItemInstance?
        get() = equipment(PlayerInventoryUtils.CHESTPLATE_SLOT) as? ArmorItemInstance

    val head: ArmorItemInstance?
        get() = equipment(PlayerInventoryUtils.HELMET_SLOT) as? ArmorItemInstance

    val finger1: AccessoryInstance?
        get() = equipment(9) as? AccessoryInstance

    val finger2: AccessoryInstance?
        get() = equipment(10) as? AccessoryInstance

    val wrist: AccessoryInstance?
        get() = equipment(11) as? AccessoryInstance

    val trinket: AccessoryInstance?
        get() = equipment(12) as? AccessoryInstance

    fun equipment(inventorySlot: Int): EquipmentItemInstance? {
        val itemStack = inventory.getItemStack(inventorySlot)
        val itemId = itemStack.getTag(ITEM_ID_TAG) ?: return null
        val equipment = itemRegistry[itemId] as EquipmentItem
        val gems = (0..<equipment.sockets).mapNotNull {
            itemStack.getTag(socketTag(it))?.let(itemRegistry::getValue) as? Gem
        }
        return equipment.instance(gems)
    }

    fun tryUseConsumable(slot: Int) {

    }
}

class InventoryDefinition(
    @JsonProperty("weapon") val weapon: ItemInstanceDefinition?,
    @JsonProperty("head") val head: ItemInstanceDefinition?,
    @JsonProperty("chest") val chest: ItemInstanceDefinition?,
    @JsonProperty("legs") val legs: ItemInstanceDefinition?,
    @JsonProperty("feet") val feet: ItemInstanceDefinition?,
    @JsonProperty("finger_1") val finger1: ItemInstanceDefinition?,
    @JsonProperty("finger_2") val finger2: ItemInstanceDefinition?,
    @JsonProperty("wrist") val wrist: ItemInstanceDefinition?,
    @JsonProperty("trinket") val trinket: ItemInstanceDefinition?,
    @JsonProperty("action_bar") val actionBar: Array<ItemInstanceDefinition?>,
    @JsonProperty("bag") val bag: Array<ItemInstanceDefinition?>
) {
    fun toInventory(playerInventory: PlayerInventory, itemRegistry: Registry<Item>) = Inventory(
        playerInventory,
        itemRegistry
    )
}

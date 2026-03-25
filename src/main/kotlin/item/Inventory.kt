package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.resource.Registry

class Inventory(
    val weapon: WeaponInstance?,
    val offhand: EquipmentItemInstance?,
    val feet: ArmorItemInstance?,
    val legs: ArmorItemInstance?,
    val chest: ArmorItemInstance?,
    val head: ArmorItemInstance?,
    val finger1: AccessoryInstance?,
    val finger2: AccessoryInstance?,
    val wrist: AccessoryInstance?,
    val trinket: AccessoryInstance?,
    val bag: Array<ItemInstance?>
)

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
    fun toInventory(itemRegistry: Registry<Item>) = Inventory(
        weapon?.toItemInstance(itemRegistry) as? WeaponInstance,
        null,
        feet?.toItemInstance(itemRegistry) as? ArmorItemInstance,
        legs?.toItemInstance(itemRegistry) as? ArmorItemInstance,
        chest?.toItemInstance(itemRegistry) as? ArmorItemInstance,
        head?.toItemInstance(itemRegistry) as? ArmorItemInstance,
        finger1?.toItemInstance(itemRegistry) as? AccessoryInstance,
        finger2?.toItemInstance(itemRegistry) as? AccessoryInstance,
        wrist?.toItemInstance(itemRegistry) as? AccessoryInstance,
        trinket?.toItemInstance(itemRegistry) as? AccessoryInstance,
        bag.map { it?.toItemInstance(itemRegistry) }.toTypedArray()
    )
}

package com.shadowforgedmmo.engine.persistence

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.item.AccessoryInstance
import com.shadowforgedmmo.engine.item.ArmorItemInstance
import com.shadowforgedmmo.engine.item.ItemInstance
import com.shadowforgedmmo.engine.item.WeaponInstance
import com.shadowforgedmmo.engine.item.deserializeAccessoryInstance
import com.shadowforgedmmo.engine.item.deserializeArmorItemInstance
import com.shadowforgedmmo.engine.item.deserializeItemInstance
import com.shadowforgedmmo.engine.item.deserializeWeaponInstance
import com.shadowforgedmmo.engine.runtime.Runtime

class InventoryData(
    val weapon: WeaponInstance,
    val head: ArmorItemInstance,
    val chest: ArmorItemInstance,
    val legs: ArmorItemInstance,
    val feet: ArmorItemInstance,
    val finger1: AccessoryInstance,
    val finger2: AccessoryInstance,
    val wrist: AccessoryInstance,
    val trinket: AccessoryInstance,
    val actionBar: Array<ItemInstance>,
    val bag: Array<ItemInstance>
)

fun deserializeInventoryData(data: JsonNode, runtime: Runtime) = InventoryData(
    deserializeWeaponInstance(data["weapon"], runtime),
    deserializeArmorItemInstance(data["head"], runtime),
    deserializeArmorItemInstance(data["chest"], runtime),
    deserializeArmorItemInstance(data["legs"], runtime),
    deserializeArmorItemInstance(data["feet"], runtime),
    deserializeAccessoryInstance(data["finger_1"], runtime),
    deserializeAccessoryInstance(data["finger_2"], runtime),
    deserializeAccessoryInstance(data["wrist"], runtime),
    deserializeAccessoryInstance(data["trinket"], runtime),
    data["action_bar"].map { deserializeItemInstance(it, runtime) }.toTypedArray(),
    data["bag"].map { deserializeItemInstance(it, runtime) }.toTypedArray()
)

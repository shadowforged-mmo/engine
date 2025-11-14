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
    val weapon: WeaponInstance?,
    val head: ArmorItemInstance?,
    val chest: ArmorItemInstance?,
    val legs: ArmorItemInstance?,
    val feet: ArmorItemInstance?,
    val finger1: AccessoryInstance?,
    val finger2: AccessoryInstance?,
    val wrist: AccessoryInstance?,
    val trinket: AccessoryInstance?,
    val actionBar: Array<ItemInstance?>,
    val bag: Array<ItemInstance?>
)

fun deserializeInventoryData(data: JsonNode, runtime: Runtime) = InventoryData(
    data["weapon"].takeUnless(JsonNode::isNull)?.let { deserializeWeaponInstance(it, runtime) },
    data["head"].takeUnless(JsonNode::isNull)?.let { deserializeArmorItemInstance(it, runtime) },
    data["chest"].takeUnless(JsonNode::isNull)?.let { deserializeArmorItemInstance(it, runtime) },
    data["legs"].takeUnless(JsonNode::isNull)?.let { deserializeArmorItemInstance(it, runtime) },
    data["feet"].takeUnless(JsonNode::isNull)?.let { deserializeArmorItemInstance(it, runtime) },
    data["finger_1"].takeUnless(JsonNode::isNull)?.let { deserializeAccessoryInstance(it, runtime) },
    data["finger_2"].takeUnless(JsonNode::isNull)?.let { deserializeAccessoryInstance(it, runtime) },
    data["wrist"].takeUnless(JsonNode::isNull)?.let { deserializeAccessoryInstance(it, runtime) },
    data["trinket"].takeUnless(JsonNode::isNull)?.let { deserializeAccessoryInstance(it, runtime) },
    data["action_bar"]
        .map { if (it.isNull) deserializeItemInstance(it, runtime) else null }
        .toTypedArray(),
    data["bag"]
        .map { if (it.isNull) deserializeItemInstance(it, runtime) else null }
        .toTypedArray()
)

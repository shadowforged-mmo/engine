package com.shadowforgedmmo.engine.item

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.model.BlockbenchItemModel
import com.shadowforgedmmo.engine.model.WeaponModel
import com.shadowforgedmmo.engine.model.deserializeWeaponModel
import com.shadowforgedmmo.engine.resource.deserializeEnum
import com.shadowforgedmmo.engine.runtime.Runtime
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class Weapon(
    id: String,
    name: String,
    quality: ItemQuality,
    val weaponType: WeaponType,
    sockets: Int,
    val model: WeaponModel
) : EquipmentItem(id, name, quality, sockets) {
    override fun instance(gems: List<Gem>) = WeaponInstance(this, gems)
}

enum class WeaponType {
    SWORD
}

class WeaponInstance(item: Weapon, gems: List<Gem>) : EquipmentItemInstance(item, gems) {
    override fun itemStack(pc: PlayerCharacter) = ItemStack.builder(Material.IRON_SWORD)
        .set(ITEM_ID_TAG, item.id)
        .customName(
            Component.text(item.name, item.quality.color).decoration(TextDecoration.ITALIC, false)
        )
        .build()
}

fun deserializeWeapon(
    id: String,
    data: JsonNode,
    blockbenchItemModelsById: Map<String, BlockbenchItemModel>
) = Weapon(
    id,
    data["name"].asText(),
    deserializeEnum(data["quality"]),
    deserializeEnum(data["weapon_type"]),
    data["sockets"]?.asInt() ?: 0,
    deserializeWeaponModel(data["model"], blockbenchItemModelsById)
)

fun deserializeWeaponInstance(data: JsonNode, runtime: Runtime) =
    deserializeItemInstance(data, runtime) as WeaponInstance

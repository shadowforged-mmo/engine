package com.shadowforgedmmo.engine.item

import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.persistence.InventoryData
import net.minestom.server.inventory.PlayerInventory
import net.minestom.server.utils.inventory.PlayerInventoryUtils

class Inventory(private val pc: PlayerCharacter, data: InventoryData) {
    init {
        val weaponInstance = WeaponInstance(
            pc.runtime.itemsById.getValue(data.weapon) as Weapon,
            emptyList() // TODO
        )
        inventory.setItemStack(8, weaponInstance.itemStack(pc))
    }

    private val inventory: PlayerInventory
        get() = pc.entity.inventory

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
        val equipment = pc.runtime.itemsById.getValue(itemId) as EquipmentItem
        val gems = (0..<equipment.sockets).mapNotNull {
            itemStack.getTag(socketTag(it))?.let(pc.runtime.itemsById::getValue) as? Gem
        }
        return equipment.instance(gems)
    }

    fun tryUseConsumable(slot: Int) {

    }
}

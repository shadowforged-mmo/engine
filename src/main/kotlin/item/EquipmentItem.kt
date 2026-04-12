package com.shadowforgedmmo.engine.item

abstract class EquipmentItem(
    id: String,
    name: String,
    quality: ItemQuality,
    val sockets: Int
) : Item(id, name, quality) {
    abstract fun instance(gems: List<Gem>): EquipmentItemInstance
}

abstract class EquipmentItemInstance(val gems: List<Gem>) : ItemInstance() {
    override val quantity: Int
        get() = 1
}

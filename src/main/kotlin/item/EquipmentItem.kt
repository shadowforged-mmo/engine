package com.shadowforgedmmo.engine.item

abstract class EquipmentItem(
    id: String,
    name: String,
    quality: ItemQuality,
    val sockets: Int
) : Item(id, name, quality) {
    abstract fun instance(socketables: List<Socketable>): EquipmentItemInstance
}

abstract class EquipmentItemInstance(val socketables: List<Socketable>) : ItemInstance() {
    override val quantity: Int
        get() = 1
}

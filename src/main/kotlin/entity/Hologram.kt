package com.shadowforgedmmo.engine.entity

import net.kyori.adventure.text.Component
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.MetadataDef
import net.minestom.server.entity.metadata.other.ArmorStandMeta

class Hologram : Entity(EntityType.ARMOR_STAND) {
    var text
        get() = metadata.get(MetadataDef.CUSTOM_NAME)
        set(value) {
            metadata.set(MetadataDef.CUSTOM_NAME, value)
        }

    init {
        val meta = entityMeta as ArmorStandMeta
        meta.isMarker = true
        meta.isHasNoGravity = true
        metadata.set(MetadataDef.CUSTOM_NAME, Component.empty())
        meta.isCustomNameVisible = true
        meta.isInvisible = true
    }
}

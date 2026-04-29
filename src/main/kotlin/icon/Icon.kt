package com.shadowforgedmmo.engine.icon

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.pack.Namespaces
import com.shadowforgedmmo.engine.resource.ICONS
import com.shadowforgedmmo.engine.resource.ResourceReference
import com.shadowforgedmmo.engine.resource.ResourceReferenceDeserializer
import net.minestom.server.item.ItemStack
import java.io.File

private fun cooldownIndex(cooldownPercent: Double) = (cooldownPercent * 16.0).toInt()

class Icon(val id: String) {
    // TODO: make sure this works for nested directories
    fun apply(itemStack: ItemStack.Builder) = itemStack.itemModel("${Namespaces.ICONS}:$id")

    fun apply(itemStack: ItemStack.Builder, cooldownPercent: Double): ItemStack.Builder {
        val cooldownIndex = cooldownIndex(cooldownPercent)
        return if (cooldownIndex == 0) {
            apply(itemStack)
        } else {
            itemStack.itemModel("${Namespaces.ICONS}:$id-${cooldownIndex}")
        }
    }
}

class IconAsset(val id: String, val file: File)

class IconDefinition(val file: File) {
    fun toIcon(id: String) = Icon(id)

    fun toIconAsset(id: String) = IconAsset(id, file)
}

@JsonDeserialize(using = IconReferenceDeserializer::class)
class IconReference(id: String) : ResourceReference(id)

class IconReferenceDeserializer : ResourceReferenceDeserializer<IconReference>(
    ICONS,
    ::IconReference
)

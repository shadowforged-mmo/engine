package com.shadowforgedmmo.engine.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.resource.Registry
import net.minestom.server.item.ItemStack
import net.minestom.server.item.armor.TrimMaterial
import net.minestom.server.item.armor.TrimPattern

abstract class ArmorModel {
    abstract val itemStack: ItemStack
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = BasicArmorModelDefinition::class, name = "basic"),
    JsonSubTypes.Type(value = BlockbenchArmorModelDefinition::class, name = "blockbench")
)
sealed class ArmorModelDefinition {
    abstract fun toArmorModel(blockbenchItemModelRegistry: Registry<BlockbenchItemModel>): ArmorModel
}

class BasicArmorModel(val material: ArmorMaterial, val trim: ArmorTrim) : ArmorModel() {
    override val itemStack = ItemStack.builder(TODO())
        .build()
}

data class BasicArmorModelDefinition(
    @JsonProperty("material") val material: ArmorMaterial,
    @JsonProperty("trim") val trim: ArmorTrimDefinition
) : ArmorModelDefinition() {
    override fun toArmorModel(blockbenchItemModelRegistry: Registry<BlockbenchItemModel>) =
        BasicArmorModel(material, trim.toTrim())
}

enum class ArmorMaterial { LEATHER, IRON }

class ArmorTrim(val material: TrimMaterial, val pattern: TrimPattern)

class ArmorTrimDefinition(
    // TODO: may want to update the types
    @JsonProperty("material") val material: String,
    @JsonProperty("pattern") val pattern: String
) {
    fun toTrim() = ArmorTrim(TODO(), TODO())
}

class BlockbenchArmorModel(val model: BlockbenchItemModel) : ArmorModel() {
    override val itemStack
        get() = model.itemStack
}

data class BlockbenchArmorModelDefinition(
    @JsonProperty("model") val model: BlockbenchItemModelReference
) : ArmorModelDefinition() {
    override fun toArmorModel(blockbenchItemModelRegistry: Registry<BlockbenchItemModel>) =
        BlockbenchArmorModel(model.resolve(blockbenchItemModelRegistry))
}

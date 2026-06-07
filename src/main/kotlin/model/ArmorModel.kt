package com.shadowforgedmmo.engine.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.shadowforgedmmo.engine.resource.Registry
import net.kyori.adventure.key.Key
import net.minestom.server.component.DataComponents
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.armor.TrimMaterial
import net.minestom.server.item.armor.TrimPattern
import net.minestom.server.item.component.ArmorTrim
import net.minestom.server.registry.RegistryKey

enum class ArmorPiece(val suffix: String) {
    HELMET("helmet"),
    CHESTPLATE("chestplate"),
    LEGGINGS("leggings"),
    BOOTS("boots")
}

abstract class ArmorModel {
    abstract fun baseBuilder(piece: ArmorPiece): ItemStack.Builder
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

class BasicArmorModel(val material: ArmorMaterial, val trim: ArmorTrim?) : ArmorModel() {
    override fun baseBuilder(piece: ArmorPiece): ItemStack.Builder {
        val mat = Material.fromKey("minecraft:${material.key}_${piece.suffix}")
            ?: error("No Minecraft material for ${material.key}_${piece.suffix}")
        val builder = ItemStack.builder(mat)
        if (trim != null) builder.set(DataComponents.TRIM, trim)
        return builder
    }
}

data class BasicArmorModelDefinition(
    @JsonProperty("material") val material: ArmorMaterial,
    @JsonProperty("trim") val trim: ArmorTrimDefinition?
) : ArmorModelDefinition() {
    override fun toArmorModel(blockbenchItemModelRegistry: Registry<BlockbenchItemModel>) =
        BasicArmorModel(material, trim?.toTrim())
}

enum class ArmorMaterial(val key: String) { LEATHER("leather"), IRON("iron") }

class ArmorTrimDefinition(
    @JsonProperty("material") val material: String,
    @JsonProperty("pattern") val pattern: String
) {
    fun toTrim(): ArmorTrim = ArmorTrim(
        RegistryKey.unsafeOf<TrimMaterial>(Key.key(material)),
        RegistryKey.unsafeOf<TrimPattern>(Key.key(pattern))
    )
}

class BlockbenchArmorModel(val model: BlockbenchItemModel) : ArmorModel() {
    override fun baseBuilder(piece: ArmorPiece) = ItemStack.builder(Material.DIAMOND).let(model::apply)
}

data class BlockbenchArmorModelDefinition(
    @JsonProperty("model") val model: BlockbenchItemModelReference
) : ArmorModelDefinition() {
    override fun toArmorModel(blockbenchItemModelRegistry: Registry<BlockbenchItemModel>) =
        BlockbenchArmorModel(model.resolve(blockbenchItemModelRegistry))
}

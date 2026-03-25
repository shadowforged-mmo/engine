package com.shadowforgedmmo.engine.character

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.shadowforgedmmo.engine.entity.EntityHuman
import com.shadowforgedmmo.engine.minecraft.MinecraftReference
import com.shadowforgedmmo.engine.model.*
import com.shadowforgedmmo.engine.resource.MINECRAFT
import com.shadowforgedmmo.engine.resource.Registry
import com.shadowforgedmmo.engine.util.loadJsonResource
import net.kyori.adventure.key.Key
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityCreature
import net.minestom.server.entity.EntityType
import team.unnamed.hephaestus.minestom.ModelEntity

val hitboxEntityTypes = loadJsonResource(
    "data/hitbox_entity_types.json",
    Array<String>::class
).map { EntityType.fromKey(it) ?: error("Missing entity type $it") }

abstract class CharacterModel {
    abstract fun createEntity(): EntityCreature
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = BlockbenchCharacterModelDefinition::class, name = "blockbench"),
    JsonSubTypes.Type(value = SkinCharacterModelDefinition::class, name = "skin"),
    JsonSubTypes.Type(value = EntityCharacterModelDefinition::class, name = "entity")
)
sealed class CharacterModelDefinition {
    abstract fun toCharacterModel(
        blockbenchModelRegistry: Map<String, BlockbenchModel>,
        skinRegistry: Registry<Skin>,
        blockbenchItemModelRegistry: Registry<BlockbenchItemModel>
    ): CharacterModel
}

class BlockbenchCharacterModel(
    val blockbenchModel: BlockbenchModel,
    val scale: Float
) : CharacterModel() {
    val hitboxEntityType = chooseHitboxEntityType()

    private fun chooseHitboxEntityType(): EntityType {
        val modelDims = blockbenchModel.model.boundingBox().multiply(scale)
        val modelWidth = modelDims.x().toDouble()
        val modelHeight = modelDims.y().toDouble()
        return hitboxEntityTypes.maxBy {
            val entityWidth = it.width()
            val entityHeight = it.height()
            2 * minOf(modelWidth, entityWidth) / maxOf(modelWidth, entityWidth) +
                    minOf(modelHeight, entityHeight) / maxOf(modelHeight, entityHeight)
        }
    }

    override fun createEntity() = BlockbenchCharacterModelEntity(this)
}

data class BlockbenchCharacterModelDefinition(
    @JsonProperty("scale") val scale: Float?,
    @JsonProperty("model") val blockbenchModelReference: BlockbenchModelReference

) : CharacterModelDefinition() {
    override fun toCharacterModel(
        blockbenchModelRegistry: Map<String, BlockbenchModel>,
        skinRegistry: Registry<Skin>,
        blockbenchItemModelRegistry: Registry<BlockbenchItemModel>
    ) = BlockbenchCharacterModel(
        blockbenchModelReference.resolve(blockbenchModelRegistry),
        scale = 1.0F
    )
}

class BlockbenchCharacterModelEntity(characterModel: BlockbenchCharacterModel) : ModelEntity(
    EntityType.TADPOLE,
    characterModel.blockbenchModel.model,
    characterModel.scale
) {
    private val hitbox = Entity(characterModel.hitboxEntityType)

    init {
        val width = boundingBox.width() * characterModel.scale
        val height = boundingBox.height() * characterModel.scale
        setBoundingBox(width, height, width)
        setNoGravity(false)
        hitbox.isInvisible = true
    }

    override fun tickAnimations() = animationPlayer().tick(position.yaw(), 0.0F)

    fun spawnHitbox() {
        hitbox.setInstance(instance, position).join()
        addPassenger(hitbox)
    }

    fun removeHitbox() = hitbox.remove()
}

class SkinCharacterModel(
    private val skin: Skin,
    private val equipment: CharacterModelEquipment = CharacterModelEquipment()
) : CharacterModel() {
    override fun createEntity(): EntityCreature {
        val entity = EntityHuman(skin)
        equipment.apply(entity)
        return entity
    }
}

data class SkinCharacterModelDefinition(
    @JsonProperty("skin") val skin: SkinReference,
    @JsonProperty("equipment") val equipment: CharacterModelEquipmentDefinition?
) : CharacterModelDefinition() {
    override fun toCharacterModel(
        blockbenchModelRegistry: Map<String, BlockbenchModel>,
        skinRegistry: Registry<Skin>,
        blockbenchItemModelRegistry: Registry<BlockbenchItemModel>
    ) = SkinCharacterModel(
        skin.resolve(skinRegistry),
        equipment?.toCharacterModelEquipment(blockbenchItemModelRegistry) ?: CharacterModelEquipment()
    )
}

class EntityCharacterModel(
    private val entityType: EntityType,
    private val equipment: CharacterModelEquipment
) : CharacterModel() {
    override fun createEntity(): EntityCreature {
        val entity = EntityCreature(entityType)
        equipment.apply(entity)
        return entity
    }
}

data class EntityCharacterModelDefinition(
    @JsonProperty("entity") val entityType: MinecraftReference,
    @JsonProperty("equipment") val equipment: CharacterModelEquipmentDefinition?
) : CharacterModelDefinition() {
    override fun toCharacterModel(
        blockbenchModelRegistry: Map<String, BlockbenchModel>,
        skinRegistry: Registry<Skin>,
        blockbenchItemModelRegistry: Registry<BlockbenchItemModel>
    ) = EntityCharacterModel(
        EntityType.fromKey(Key.key(MINECRAFT, entityType.id)) ?: error("Unknown entity type: ${entityType.id}"),
        equipment?.toCharacterModelEquipment(blockbenchItemModelRegistry) ?: CharacterModelEquipment()
    )
}

class CharacterModelEquipment(
    val mainHand: BlockbenchItemModel? = null,
    val offHand: BlockbenchItemModel? = null,
    val feet: ArmorModel? = null,
    val legs: ArmorModel? = null,
    val chest: ArmorModel? = null,
    val head: ArmorModel? = null
) {
    fun apply(entity: EntityCreature) {
        mainHand?.let { entity.itemInMainHand = it.itemStack }
        offHand?.let { entity.itemInOffHand = it.itemStack }
        feet?.let { entity.boots = it.itemStack }
        legs?.let { entity.leggings = it.itemStack }
        chest?.let { entity.chestplate = it.itemStack }
        head?.let { entity.helmet = it.itemStack }
    }
}

data class CharacterModelEquipmentDefinition(
    @JsonProperty("main_hand") val mainHand: BlockbenchItemModelReference?,
    @JsonProperty("off_hand") val offHand: BlockbenchItemModelReference?,
    @JsonProperty("feet") val feet: ArmorModelDefinition?,
    @JsonProperty("legs") val legs: ArmorModelDefinition?,
    @JsonProperty("chest") val chest: ArmorModelDefinition?,
    @JsonProperty("head") val head: ArmorModelDefinition?
) {
    fun toCharacterModelEquipment(blockbenchItemModelRegistry: Registry<BlockbenchItemModel>) =
        CharacterModelEquipment(
            mainHand?.resolve(blockbenchItemModelRegistry),
            offHand?.resolve(blockbenchItemModelRegistry),
            feet?.toArmorModel(blockbenchItemModelRegistry),
            legs?.toArmorModel(blockbenchItemModelRegistry),
            chest?.toArmorModel(blockbenchItemModelRegistry),
            head?.toArmorModel(blockbenchItemModelRegistry),
        )
}

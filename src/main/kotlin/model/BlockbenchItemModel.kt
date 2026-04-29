package com.shadowforgedmmo.engine.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.pack.Namespaces
import com.shadowforgedmmo.engine.resource.ITEM_MODELS
import com.shadowforgedmmo.engine.resource.ResourceReference
import com.shadowforgedmmo.engine.resource.ResourceReferenceDeserializer
import com.shadowforgedmmo.engine.util.readJsonTree
import net.kyori.adventure.key.Key
import net.minestom.server.item.ItemStack
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.base.Axis3D
import team.unnamed.creative.base.CubeFace
import team.unnamed.creative.base.Vector3Float
import team.unnamed.creative.base.Writable
import team.unnamed.creative.item.Item
import team.unnamed.creative.item.ItemModel
import team.unnamed.creative.model.*
import team.unnamed.creative.texture.Texture
import team.unnamed.creative.texture.TextureUV
import java.io.File
import java.util.*

private const val TEXTURE_DATA_PREFIX = "data:image/png;base64,"

class BlockbenchItemModel(val id: String) {
    fun apply(itemStack: ItemStack.Builder) = itemStack.itemModel("${Namespaces.ITEM_MODELS}:$id")
}

class BlockbenchItemModelDefinition(private val file: File) {
    fun toBlockbenchItemModel(id: String) = BlockbenchItemModel(id)

    fun toBlockbenchItemModelAsset(id: String) =
        deserializeBlockbenchItemModelAsset(id, readJsonTree(file))
}

@JsonDeserialize(using = BlockbenchItemModelReferenceDeserializer::class)
class BlockbenchItemModelReference(id: String) : ResourceReference(id)

class BlockbenchItemModelReferenceDeserializer : ResourceReferenceDeserializer<BlockbenchItemModelReference>(
    ITEM_MODELS,
    ::BlockbenchItemModelReference
)

class BlockbenchItemModelAsset(
    val id: String,
    val display: Map<ItemTransform.Type, ItemTransform>,
    val textureMappings: Map<String, ModelTexture>,
    val textures: List<Texture>,
    val elements: List<Element>
) {
    fun write(pack: ResourcePack) {
        val key = Key.key(Namespaces.ITEM_MODELS, "item/$id")
        val keyWithoutPrefix = Key.key(Namespaces.ITEM_MODELS, id)

        textures.forEach(pack::texture)

        pack.model(
            Model.model()
                .key(key)
                .display(display)
                .textures(
                    ModelTextures.builder()
                        .variables(textureMappings)
                        .build()
                )
                .elements(elements)
                .build()
        )

        pack.item(
            Item.item(keyWithoutPrefix, ItemModel.reference(key))
        )
    }
}

private fun deserializeBlockbenchItemModelAsset(id: String, json: JsonNode): BlockbenchItemModelAsset {
    val display = json["display"]?.let(::deserializeDisplay) ?: emptyMap()
    val resolution = json["resolution"]
    val textureWidth = resolution["width"].intValue()
    val textureHeight = resolution["height"].intValue()
    val (textures, textureMappings) = deserializeTextures(json["textures"], id)
    val elements = json["elements"].map { deserializeElement(it, textureWidth, textureHeight) }
    return BlockbenchItemModelAsset(id, display, textureMappings, textures, elements)
}

private fun deserializeDisplay(json: JsonNode) = ItemTransform.Type.entries.mapNotNull { type ->
    json[type.name.lowercase()]?.let { type to deserializeItemTransform(it) }
}.toMap()

private fun deserializeItemTransform(json: JsonNode) = ItemTransform.transform().apply {
    json["translation"]?.let { translation(deserializeVec3(it)) }
    json["rotation"]?.let { rotation(deserializeVec3(it)) }
    json["scale"]?.let { scale(deserializeVec3(it)) }
}.build()

private fun deserializeTextures(
    json: JsonNode,
    modelId: String
): Pair<List<Texture>, Map<String, ModelTexture>> {
    val textures = mutableListOf<Texture>()
    val mappings = mutableMapOf<String, ModelTexture>()
    json.forEachIndexed { index, textureNode ->
        val key = Key.key(Namespaces.ITEM_MODELS, "item/$modelId/$index")
        val keyWithSuffix = Key.key(Namespaces.ITEM_MODELS, "item/$modelId/$index.png")
        val source = textureNode["source"].textValue().removePrefix(TEXTURE_DATA_PREFIX)
        val bytes = Base64.getDecoder().decode(source)
        textures += Texture.texture(keyWithSuffix, Writable.bytes(bytes))
        mappings[index.toString()] = ModelTexture.ofKey(key)
    }
    return textures to mappings
}

private fun deserializeElement(json: JsonNode, textureWidth: Int, textureHeight: Int): Element {
    val builder = Element.element()
        .from(deserializeVec3(json["from"]))
        .to(deserializeVec3(json["to"]))
    json["rotation"]?.let { builder.rotation(deserializeElementRotation(json, it)) }
    json["faces"]?.let { faces ->
        CubeFace.entries.forEach { face ->
            faces[face.name.lowercase()]?.let {
                builder.addFace(face, deserializeElementFace(it, textureWidth, textureHeight))
            }
        }
    }
    return builder.build()
}

private fun deserializeElementRotation(json: JsonNode, rotation: JsonNode): ElementRotation {
    val origin = deserializeVec3(json["origin"])
    val axes = listOf(Axis3D.X, Axis3D.Y, Axis3D.Z).zip(rotation.map(JsonNode::floatValue))
    val (axis, angle) = axes.firstOrNull { (_, a) -> a != 0f } ?: axes.last()
    return ElementRotation.of(origin, axis, angle, ElementRotation.DEFAULT_RESCALE)
}

private fun deserializeElementFace(json: JsonNode, textureWidth: Int, textureHeight: Int): ElementFace {
    val uv = json["uv"]
    val textureId = json["texture"].intValue()
    return ElementFace.face()
        .uv(
            TextureUV.uv(
                uv[0].floatValue() / textureWidth,
                uv[1].floatValue() / textureHeight,
                uv[2].floatValue() / textureWidth,
                uv[3].floatValue() / textureHeight
            )
        )
        .texture("#$textureId")
        .build()
}

private fun deserializeVec3(json: JsonNode) = Vector3Float(
    json[0].floatValue(),
    json[1].floatValue(),
    json[2].floatValue()
)

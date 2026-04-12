package com.shadowforgedmmo.engine.pack

import com.shadowforgedmmo.engine.icon.IconAsset
import com.shadowforgedmmo.engine.model.BlockbenchItemModelAsset
import com.shadowforgedmmo.engine.model.BlockbenchModel
import com.shadowforgedmmo.engine.resource.ResourcePackResources
import com.shadowforgedmmo.engine.util.loadJsonResource
import net.kyori.adventure.key.Key
import net.minestom.server.item.Material
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.base.Writable
import team.unnamed.creative.item.Item
import team.unnamed.creative.item.ItemModel
import team.unnamed.creative.model.ItemOverride
import team.unnamed.creative.model.Model
import team.unnamed.creative.model.ModelTexture
import team.unnamed.creative.model.ModelTextures
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackWriter
import team.unnamed.creative.sound.Sound
import team.unnamed.creative.sound.SoundEntry
import team.unnamed.creative.sound.SoundEvent
import team.unnamed.creative.sound.SoundRegistry
import team.unnamed.creative.texture.Texture
import team.unnamed.hephaestus.writer.ModelWriter
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.atan2


class PackBuilder(private val resources: ResourcePackResources) {
    private val pack = ResourcePack.resourcePack()

    fun build() {
        writePackMeta()
        writeBasePack()
        writeEngineModels()
        writeEngineSoundAssets()
        writeModels()
        writeItemModels()
        writeMusic()
        writeSoundAssets()
        disableMinecraftMusic()
        writeIcons()
        savePack()
    }

    private fun writePackMeta() {
        pack.packMeta(46, "${resources.config.name} resource pack")
    }

    private fun writeBasePack() {
        writeBaseTextures()
    }

    private fun writeBaseTextures() {
        listOf("action_bar_item_empty", "action_bar_skill_empty").forEach { id ->
            writeTexture(
                Namespaces.TEXTURES,
                id,
                Writable.resource(javaClass.classLoader, "textures/$id.png")
            )
        }
    }

    private fun writeEngineModels() {
        // TODO
    }

    private fun writeEngineSoundAssets() {
        listOf("quest_start", "quest_complete").forEach {
            val key = Key.key(Namespaces.ENGINE_SOUNDS, it)
            val sound = Sound.sound(
                key,
                Writable.resource(this::class.java.classLoader, "sounds/$it.ogg")
            )
            val soundEvent = SoundEvent.soundEvent(
                key,
                false,
                null,
                listOf(SoundEntry.soundEntry(sound))
            )
            pack.sound(sound)
            pack.soundEvent(soundEvent)
        }
    }

    private fun writeModels() {
        val models = resources.blockbenchModels.values.map(BlockbenchModel::model)
        ModelWriter.resource(Namespaces.MODELS).write(pack, models)
    }

    private fun writeItemModels() {
        // TODO
    }

    private fun writeBlockbenchItemModel(
        blockbenchItemModelAsset: BlockbenchItemModelAsset,
        overrides: MutableList<ItemOverride>
    ) {
//        pack.model(
//            Model.model()
//                .key(key)
//                .display(display)
//                .textures(textures)
//                .elements(elements)
//                .build()
//        )
//        model.textures.forEach(pack::texture)
//        overrides += ItemOverride.of(
//            key,
//            ItemPredicate.customModelData(itemModelAsset.customModelData)
//        )
    }

    private fun writeItemOverrides(
        material: Material,
        overrides: List<ItemOverride>
    ) {
        pack.model(
            Model.model()
                .key(material.key())
                .parent(Key.key(Namespaces.MINECRAFT, "item/handheld"))
                .textures(
                    ModelTextures.builder()
                        .layers(ModelTexture.ofKey(material.key()))
                        .build()
                )
                .overrides(overrides)
                .build()
        )
    }

    private fun writeMusic() {
        resources.musicTrackAssets.values.forEach { songAsset ->
            val sound = Sound.sound(songAsset.key, Writable.file(songAsset.file))
            val soundEvent = SoundEvent.soundEvent(
                songAsset.key,
                false,
                null,
                listOf(SoundEntry.soundEntry(sound))
            )
            pack.sound(sound)
            pack.soundEvent(soundEvent)
        }
    }

    private fun writeSoundAssets() {
        resources.soundAssets.values.forEach {
            val sound = Sound.sound(
                it.key,
                Writable.file(it.file)
            )
            val soundEvent = SoundEvent.soundEvent(
                it.key,
                false,
                null,
                listOf(SoundEntry.soundEntry(sound))
            )
            pack.sound(sound)
            pack.soundEvent(soundEvent)
        }
    }

    private fun disableMinecraftMusic() {
        val minecraftMusic = loadJsonResource(
            "data/minecraft_music.json",
            Array<String>::class
        )
        val soundEvents = minecraftMusic.map { song ->
            SoundEvent.soundEvent(
                Key.key(Namespaces.MINECRAFT, song),
                true,
                null,
                emptyList()
            )
        }
        pack.soundRegistry(
            SoundRegistry.soundRegistry(
                Namespaces.MINECRAFT,
                soundEvents
            )
        )
    }

    private fun writeIcons() {
        resources.iconAssets.values.forEach { iconAsset ->
            writeTexture(Namespaces.ICONS, iconAsset.id, iconAsset.file)
            if (iconAsset in resources.iconAssetsWithCooldowns) {
                writeCooldownIcons(iconAsset)
            }
        }
    }

    private fun writeCooldownIcons(iconAsset: IconAsset) {
        val image = ImageIO.read(iconAsset.file)
        // TODO: skip writing texture that's identical to base
        (0..16).forEach { cooldownIdx ->
            val cooldownImage = createCooldownImage(image, cooldownIdx)
            writeTexture(Namespaces.ICONS, "${iconAsset.id}-$cooldownIdx", cooldownImage)
        }
    }

    private fun createCooldownImage(image: BufferedImage, cooldownIdx: Int): BufferedImage {
        val cooldownImage = BufferedImage(
            image.width,
            image.height,
            BufferedImage.TYPE_INT_ARGB
        )

        val thetaMax: Double = cooldownIdx / 16.0 * 2.0 * Math.PI // TODO: make 16 a constant somewhere

        val cx = image.width / 2.0
        val cy = image.height / 2.0

        for (x in 0..<image.width) {
            for (y in 0..<image.height) {
                var theta = atan2(cy - y, x - cx) - Math.PI / 2.0
                if (theta < 0.0) {
                    theta += 2.0 * Math.PI
                }

                if (theta < thetaMax) {
                    val color = Color(image.getRGB(x, y), true)
                    val gray = ((color.red + color.green + color.blue) / 3.0 * 0.25).toInt()
                    val cooldownColor = Color(gray, gray, gray, color.alpha)
                    cooldownImage.setRGB(x, y, cooldownColor.rgb)
                } else {
                    cooldownImage.setRGB(x, y, image.getRGB(x, y))
                }
            }
        }

        return cooldownImage
    }

    private fun writeTexture(
        namespace: String,
        id: String,
        file: File
    ) = writeTexture(namespace, id, Writable.file(file))

    private fun writeTexture(
        namespace: String,
        id: String,
        image: BufferedImage
    ) = writeTexture(
        namespace,
        id,
        Writable.bytes(ByteArrayOutputStream().use { output ->
            ImageIO.write(image, "png", output)
            output.toByteArray()
        })
    )

    private fun writeTexture(namespace: String, id: String, textureData: Writable) {
        val key = Key.key(namespace, "item/$id")
        val keyWithSuffix = Key.key(namespace, "item/$id.png")
        val keyWithoutPrefix = Key.key(namespace, id)

        pack.texture(
            Texture.texture(
                keyWithSuffix,
                textureData
            )
        )

        pack.model(
            Model.model()
                .key(key)
                .parent(Key.key(Namespaces.MINECRAFT, "item/generated"))
                .textures(
                    ModelTextures.builder().addLayer(ModelTexture.ofKey(key))
                        .build()
                )
                .build()
        )

        pack.item(
            Item.item(keyWithoutPrefix, ItemModel.reference(key))
        )
    }

    private fun savePack() {
        MinecraftResourcePackWriter.minecraft().writeToZipFile(
            File("pack.zip"),
            pack
        )
    }
}

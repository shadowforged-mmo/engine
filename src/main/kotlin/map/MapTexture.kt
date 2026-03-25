package com.shadowforgedmmo.engine.map

import java.awt.image.BufferedImage

class MapTexture(val id: String, val texture: BufferedImage)

// TODO: remove
//fun deserializeMapTexture(id: String, file: File) = MapTexture(id, ImageIO.read(file))
//
//fun parseMapTextureId(id: String) = parseId(id, "map_textures")

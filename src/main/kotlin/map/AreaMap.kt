package com.shadowforgedmmo.engine.map

import com.shadowforgedmmo.engine.math.Vector2

data class AreaMap(
    val id: String,
    val origin: Vector2,
    val texture: MapTexture
)

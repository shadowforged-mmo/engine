package com.shadowforgedmmo.engine.time

import kotlin.math.max
import kotlin.math.min

class Cooldown(private val durationMillis: Long) {
    private var lastSetMillis = 0L

    fun hasCooldown(timeMillis: Long) = timeMillis - lastSetMillis < durationMillis

    fun set(timeMillis: Long) {
        lastSetMillis = timeMillis
    }

    fun remainingMillis(timeMillis: Long) = max(0L, durationMillis - (timeMillis - lastSetMillis))

    fun progress(timeMillis: Long) = min(
        1F - (timeMillis - lastSetMillis).toFloat() / durationMillis.toFloat(),
        1F
    )
}

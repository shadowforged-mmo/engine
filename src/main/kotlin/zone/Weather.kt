package com.shadowforgedmmo.engine.zone

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.time.secondsToMillis

data class WeatherCycleEntry(val durationMillis: Long, val weather: Weather)

data class WeatherCycleEntryDefinition(
    @JsonProperty("duration") val durationSeconds: Double,
    @JsonProperty("weather") val weather: String // TODO
) {
    fun toWeatherCycleEntry() = WeatherCycleEntry(
        secondsToMillis(durationSeconds),
        Weather(0.0F, 0.0F) // TODO
    )
}

data class Weather(val rain: Float, val thunder: Float) {
    init {
        require(rain in 0.0F..1.0F)
        require(thunder in 0.0F..1.0F)
    }

    companion object {
        val CLEAR = Weather(0.0F, 0.0F)
        val RAIN = Weather(1.0F, 0.0F)
        val THUNDER = Weather(1.0F, 1.0F)
    }
}

package com.shadowforgedmmo.engine.persistence

import com.shadowforgedmmo.engine.math.Position

class PlayerCharacterData(
    val instanceId: String,
    val position: Position,
    val maxHealth: Double,
    val health: Double,
    val playerClassId: String,
    val questTrackerData: QuestTrackerData,
    val zoneId: String
)

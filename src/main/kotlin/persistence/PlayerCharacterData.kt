package com.shadowforgedmmo.engine.persistence

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.instance.Instance
import com.shadowforgedmmo.engine.instance.parseInstanceId
import com.shadowforgedmmo.engine.math.Position
import com.shadowforgedmmo.engine.math.deserializePosition
import com.shadowforgedmmo.engine.playerclass.PlayerClass
import com.shadowforgedmmo.engine.playerclass.parsePlayerClassId
import com.shadowforgedmmo.engine.runtime.Runtime
import com.shadowforgedmmo.engine.zone.Zone
import com.shadowforgedmmo.engine.zone.parseZoneId

class PlayerCharacterData(
    val playerClass: PlayerClass,
    val instance: Instance,
    val position: Position,
    val zone: Zone,
    val maxHealth: Double,
    val health: Double,
    val maxMana: Double,
    val mana: Double,
    val quests: QuestData,
    val inventory: InventoryData,
)

fun deserializePlayerCharacterData(data: JsonNode, runtime: Runtime) = PlayerCharacterData(
    runtime.playerClassesById.getValue(parsePlayerClassId(data["class"].asText())),
    runtime.instancesById.getValue(parseInstanceId(data["instance"].asText())),
    deserializePosition(data["position"]),
    runtime.zonesById.getValue(parseZoneId(data["zone"].asText())),
    data["max_health"].asDouble(),
    data["health"].asDouble(),
    data["max_mana"].asDouble(),
    data["mana"].asDouble(),
    deserializeQuestData(data["quests"], runtime),
    deserializeInventoryData(data["inventory"], runtime)
)

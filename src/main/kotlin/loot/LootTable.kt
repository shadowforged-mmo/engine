package com.shadowforgedmmo.engine.loot

import com.fasterxml.jackson.databind.JsonNode

class LootTable {
}

fun deserializeLootTable(data: JsonNode): LootTable {
    return LootTable()
}

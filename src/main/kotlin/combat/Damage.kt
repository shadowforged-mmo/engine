package com.shadowforgedmmo.engine.combat

import com.fasterxml.jackson.databind.JsonNode

class Damage(val damage: Map<DamageType, Int>)

enum class DamageType {
    PHYSICAL,
    ARCANE,
    FIRE,
    FROST,
    NATURE,
    SHADOW,
    HOLY
}

fun deserializeDamage(data: JsonNode): Damage =
    if (data.isNumber) {
        Damage(mapOf(DamageType.PHYSICAL to data.asInt()))
    } else if (data.isObject) {
        Damage(data.fields().asSequence().associate { (type, amount) ->
            DamageType.valueOf(type.uppercase()) to amount.asInt()
        })
    } else {
        throw IllegalArgumentException()
    }

package com.shadowforgedmmo.engine.combat

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.resource.EnumDeserializer

@JsonDeserialize(using = DamageDeserializer::class)
class Damage(val damage: Map<DamageType, Int>)

class DamageDeserializer : JsonDeserializer<Damage>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): Damage {
        val node = p.codec.readTree<JsonNode>(p)
        if (node.isInt) {
            return Damage(mapOf(DamageType.PHYSICAL to node.intValue()))
        }

        // TODO: error handling and clean up
        return Damage(node.fields().asSequence().associate { (key, value) ->
            DamageType.valueOf(key) to value.intValue()
        })
    }
}

@JsonDeserialize(using = EnumDeserializer::class)
enum class DamageType {
    PHYSICAL,
    ARCANE,
    FIRE,
    FROST,
    NATURE,
    SHADOW,
    HOLY
}

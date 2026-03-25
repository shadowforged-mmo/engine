package com.shadowforgedmmo.engine.resource

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

class EnumDeserializer<T : Enum<T>>(
    private val enumClass: Class<T>
) : JsonDeserializer<T>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): T = java.lang.Enum.valueOf(enumClass, p.valueAsString.uppercase())
}
package com.shadowforgedmmo.engine.resource

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException

open class ResourceReference(val id: String) {
    fun <T> resolve(registry: Registry<T>) = registry[id] ?: throw ResourceNotFoundException(id)
}

typealias Registry<T> = Map<String, T>

class ResourceNotFoundException(resourceId: String) : RuntimeException("$resourceId not found")

open class ResourceReferenceDeserializer<R : ResourceReference>(
    val prefix: String,
    val factory: (String) -> R
) : JsonDeserializer<R>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): R {
        val text = p.valueAsString ?: throw JsonMappingException.from(p, "Resource reference must be a string")

        val expectedPrefix = "$prefix:"
        if (!text.startsWith(expectedPrefix)) {
            throw JsonMappingException.from(p, "Resource reference must start with $expectedPrefix")
        }

        val id = text.removePrefix(expectedPrefix)
        if (id.isEmpty()) {
            throw JsonMappingException.from(p, "ID must not be empty")
        }

        return factory(id)
    }
}

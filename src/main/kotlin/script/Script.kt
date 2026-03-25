package com.shadowforgedmmo.engine.script

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.resource.ResourceReference
import com.shadowforgedmmo.engine.resource.ResourceReferenceDeserializer
import com.shadowforgedmmo.engine.resource.SCRIPTS

class Script(val id: String)

class ScriptDefinition {
    fun toScript(id: String) = Script(id)
}

@JsonDeserialize(using = ScriptReferenceDeserializer::class)
class ScriptReference(id: String) : ResourceReference(id)

class ScriptReferenceDeserializer : ResourceReferenceDeserializer<ScriptReference>(
    SCRIPTS,
    ::ScriptReference
)

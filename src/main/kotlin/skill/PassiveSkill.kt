package com.shadowforgedmmo.engine.skill

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.resource.Registry
import com.shadowforgedmmo.engine.script.Script
import com.shadowforgedmmo.engine.script.ScriptReference

class PassiveSkill(
    id: String,
    name: String,
    description: String,
    script: Script
) : Skill(id, name, description, script)

data class PassiveSkillDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("script") val scriptReference: ScriptReference
) : SkillDefinition() {
    override fun toSKill(id: String, scriptRegistry: Registry<Script>) = PassiveSkill(
        id,
        name,
        description,
        scriptReference.resolve(scriptRegistry)
    )
}

package com.shadowforgedmmo.engine.skill

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.icon.Icon
import com.shadowforgedmmo.engine.icon.IconReference
import com.shadowforgedmmo.engine.resource.Registry
import com.shadowforgedmmo.engine.script.Script
import com.shadowforgedmmo.engine.script.ScriptReference

class PassiveSkill(
    id: String,
    name: String,
    icon: Icon,
    description: String,
    script: Script
) : Skill(id, name, icon, description, script)

data class PassiveSkillDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("icon") val iconReference: IconReference,
    @JsonProperty("description") val description: String,
    @JsonProperty("script") val scriptReference: ScriptReference
) : SkillDefinition() {
    override fun toSKill(
        id: String,
        iconRegistry: Registry<Icon>,
        scriptRegistry: Registry<Script>
    ) = PassiveSkill(
        id,
        name,
        iconReference.resolve(iconRegistry),
        description,
        scriptReference.resolve(scriptRegistry)
    )
}

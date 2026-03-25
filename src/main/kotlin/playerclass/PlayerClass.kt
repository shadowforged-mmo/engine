package com.shadowforgedmmo.engine.playerclass

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.resource.CLASSES
import com.shadowforgedmmo.engine.resource.Registry
import com.shadowforgedmmo.engine.resource.ResourceReference
import com.shadowforgedmmo.engine.resource.ResourceReferenceDeserializer
import com.shadowforgedmmo.engine.skill.Skill
import com.shadowforgedmmo.engine.skill.SkillReference

class PlayerClass(val id: String, val skills: List<Skill>)

data class PlayerClassDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("skills") val skills: List<SkillReference>
) {
    fun toPlayerClass(id: String, skillRegistry: Registry<Skill>) = PlayerClass(
        id,
        skills.map { it.resolve(skillRegistry) }
    )
}

@JsonDeserialize(using = PlayerClassReferenceDeserializer::class)
class PlayerClassReference(id: String) : ResourceReference(id)

class PlayerClassReferenceDeserializer : ResourceReferenceDeserializer<PlayerClassReference>(
    CLASSES,
    ::PlayerClassReference
)

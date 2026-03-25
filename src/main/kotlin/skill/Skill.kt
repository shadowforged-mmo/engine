package com.shadowforgedmmo.engine.skill

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.shadowforgedmmo.engine.resource.Registry
import com.shadowforgedmmo.engine.resource.ResourceReference
import com.shadowforgedmmo.engine.resource.ResourceReferenceDeserializer
import com.shadowforgedmmo.engine.resource.SKILLS
import com.shadowforgedmmo.engine.script.Script
import net.minestom.server.tag.Tag

val SKILL_TAG = Tag.String("skill")

abstract class Skill(
    val id: String,
    val name: String,
    val description: String,
    val script: Script
)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ActiveSkillDefinition::class, name = "active"),
    JsonSubTypes.Type(value = PassiveSkillDefinition::class, name = "passive"),
)
sealed class SkillDefinition {
    abstract fun toSKill(id: String, scriptRegistry: Registry<Script>): Skill
}

@JsonDeserialize(using = SkillReferenceDeserializer::class)
class SkillReference(id: String) : ResourceReference(id)

class SkillReferenceDeserializer : ResourceReferenceDeserializer<SkillReference>(
    SKILLS,
    ::SkillReference
)

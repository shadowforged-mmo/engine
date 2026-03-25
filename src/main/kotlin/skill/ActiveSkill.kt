package com.shadowforgedmmo.engine.skill

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.resource.Registry
import com.shadowforgedmmo.engine.script.Script
import com.shadowforgedmmo.engine.script.ScriptReference
import com.shadowforgedmmo.engine.time.millisToSeconds
import com.shadowforgedmmo.engine.time.secondsToMillis
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import kotlin.math.ceil
import kotlin.math.max

class ActiveSkill(
    id: String,
    name: String,
    description: String,
    val manaCost: Int,
    val cooldownMillis: Long,
    script: Script
) : Skill(id, name, description, script) {
    fun hotbarItemStack(pc: PlayerCharacter) = ItemStack.builder(Material.DIAMOND)
        .set(SKILL_TAG, id)
        .customName(Component.text(name, NamedTextColor.GREEN))
        .amount(
            max(
                1,
                ceil(
                    millisToSeconds(
                        pc.cooldown(this).remainingMillis(pc.runtime.timeMillis)
                    )
                ).toInt()
            )
        )
        .build()
}

data class ActiveSkillDefinition(
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("mana_cost") val manaCost: Int,
    @JsonProperty("cooldown") val cooldownSeconds: Double,
    @JsonProperty("script") val scriptReference: ScriptReference
) : SkillDefinition() {
    override fun toSKill(id: String, scriptRegistry: Registry<Script>) = ActiveSkill(
        id,
        name,
        description,
        manaCost,
        secondsToMillis(cooldownSeconds),
        scriptReference.resolve(scriptRegistry)
    )
}

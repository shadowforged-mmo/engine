package com.shadowforgedmmo.engine.skill

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.script.parseScriptId
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
    val manaCost: Double,
    val cooldownMillis: Long,
    scriptId: String
) : Skill(id, name, description, scriptId) {
    fun hotbarItemStack(pc: PlayerCharacter) = ItemStack.builder(Material.DIAMOND)
        .set(SKILL_TAG, id)
        .customName(Component.text(name, NamedTextColor.GREEN))
        .amount(
            max(
                1,
                ceil(
                    millisToSeconds(
                        pc.skillTracker.cooldown(this).remainingMillis(pc.runtime.timeMillis)
                    )
                ).toInt()
            )
        )
        .build()
}

fun deserializeActiveSkill(
    id: String,
    data: JsonNode
) = ActiveSkill(
    id,
    data["name"].asText(),
    data["description"].asText(),
    data["mana_cost"].asDouble(),
    secondsToMillis(data["cooldown"].asDouble()),
    parseScriptId(data["script"].asText())
)

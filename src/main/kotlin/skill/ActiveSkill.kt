package com.shadowforgedmmo.engine.skill

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.script.parseScriptId
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class ActiveSkill(
    id: String,
    name: String,
    description: String,
    val manaCost: Double,
    scriptId: String
) : Skill(id, name, description, scriptId) {
    fun hotbarItemStack() = ItemStack.builder(Material.DIAMOND)
        .customName(Component.text(name, NamedTextColor.GREEN))
        .build()
}

fun deserializeActiveSkill(
    id: String,
    data: JsonNode
) = ActiveSkill(
    id,
    data["name"].asText(),
    data["description"].asText(),
    data["manaCost"].asDouble(),
    parseScriptId(data["scriptId"].asText())
)

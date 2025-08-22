package com.shadowforgedmmo.engine.skill

import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.time.Cooldown
import net.kyori.adventure.text.Component
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class SkillTracker(private val pc: PlayerCharacter) {
    private val cooldowns = mutableMapOf<ActiveSkill, Cooldown>()
    private val skillExecutors = mutableListOf<SkillExecutor>()

    fun tryUseSkill(slot: Int) {
        val skill = hotbarSkill(slot) ?: return
        tryUseSkill(skill)
    }

    private fun hotbarSkill(slot: Int) = pc.entity.inventory.getItemStack(slot).getTag(SKILL_TAG)?.let {
        pc.runtime.skillsById.getValue(it) as? ActiveSkill
    }

    private fun tryUseSkill(skill: ActiveSkill) {
        val cooldown = cooldown(skill)
        if (cooldown != null)
            return failUseSkill(Component.text("On cooldown")) // TODO

        if (pc.mana < skill.manaCost)
            return failUseSkill(Component.text("Not enough mana")) // TODO

        useSkill(skill)
    }

    private fun failUseSkill(message: Component) {
        pc.sendMessage(message)
        // TODO: play sound
    }

    private fun useSkill(skill: ActiveSkill) {
        val skillExecutor = SkillExecutor(pc, skill, pc.runtime.timeMillis)
        skillExecutor.init()
        skillExecutors.add(skillExecutor)
    }

    fun tick() {
        skillExecutors.forEach(SkillExecutor::tick)
        skillExecutors.removeIf(SkillExecutor::completed)
        updateHotbar();
    }

    private fun updateHotbar() = (0..5).forEach(::updateHotbarSlot)

    private fun updateHotbarSlot(slot: Int) {
        pc.entity.inventory.setItemStack(0, (pc.playerClass.skills[0] as ActiveSkill).hotbarItemStack(pc))
        val skill = hotbarSkill(slot)
        if (skill == null) {
            pc.entity.inventory.setItemStack(slot, ItemStack.of(Material.BARRIER))
            return
        }
        pc.entity.inventory.setItemStack(slot, skill.hotbarItemStack(pc))
    }

    private fun cooldown(skill: Skill) = cooldowns[skill]
}

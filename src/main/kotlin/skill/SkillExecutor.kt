package com.shadowforgedmmo.engine.skill

import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.script.SkillExecutor as ScriptSkillExecutor

class SkillExecutor(
    val user: PlayerCharacter,
    private val skill: ActiveSkill,
    private val startTimeMillis: Long
) {
    private val handle = user.runtime.interpreter.instantiate<ScriptSkillExecutor>(skill.scriptId, this)

    var completed = false
        private set

    val lifetimeMillis
        get() = user.runtime.timeMillis - startTimeMillis

    fun init() = handle.init()

    fun tick() = handle.tick()

    fun complete() {
        completed = true
    }
}

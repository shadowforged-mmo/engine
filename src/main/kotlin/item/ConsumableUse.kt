package com.shadowforgedmmo.engine.item

import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.script.ConsumableHandler as ScriptConsumableHandler

class ConsumableUse(val user: PlayerCharacter, val item: ConsumableItem) {
    private val handle = user.runtime.interpreter.instantiate<ScriptConsumableHandler>(item.script, this)

    fun use() = handle.use()
}

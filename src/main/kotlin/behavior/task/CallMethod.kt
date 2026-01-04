package com.shadowforgedmmo.engine.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.behavior.BehaviorBlueprint
import com.shadowforgedmmo.engine.behavior.BehaviorStatus
import com.shadowforgedmmo.engine.behavior.Task
import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import org.python.core.Py
import org.python.core.PyBoolean

class CallMethod(private val methodName: String) : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val status = Py.java2py(character.handle).invoke(methodName)
        if (status is PyBoolean) {
            return if (status.booleanValue) BehaviorStatus.SUCCESS else BehaviorStatus.FAILURE
        }
        return BehaviorStatus.SUCCESS
    }
}

class CallMethodBlueprint(private val methodName: String) : BehaviorBlueprint() {
    override fun create() = CallMethod(methodName)
}

fun deserializeCallMethodBlueprint(data: JsonNode) = CallMethodBlueprint(data["method"].asText())

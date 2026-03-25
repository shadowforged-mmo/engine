package com.shadowforgedmmo.engine.behavior

import com.fasterxml.jackson.annotation.JsonProperty
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

data class CallMethodDefinition(
    @JsonProperty("method") val method: String
) : BehaviorDefinition() {
    override fun toBlueprint() =
        CallMethodBlueprint(method)
}

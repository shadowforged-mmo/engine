package com.shadowforgedmmo.engine.behavior

import com.shadowforgedmmo.engine.character.NonPlayerCharacter

class HasTarget : Behavior() {
    override fun update(character: NonPlayerCharacter) =
        character.target?.let { BehaviorStatus.SUCCESS } ?: BehaviorStatus.FAILURE
}

class HasTargetBlueprint : BehaviorBlueprint() {
    override fun create() = HasTarget()
}

class HasTargetDefinition : BehaviorDefinition() {
    override fun toBlueprint() = HasTargetBlueprint()
}

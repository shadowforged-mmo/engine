package com.shadowforgedmmo.engine.behavior

import com.shadowforgedmmo.engine.character.NonPlayerCharacter

class FaceTarget : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val target = character.target ?: return BehaviorStatus.FAILURE
        character.lookAt(target)
        return BehaviorStatus.SUCCESS
    }
}

class FaceTargetBlueprint : BehaviorBlueprint() {
    override fun create() = FaceTarget()
}

class FaceTargetDefinition : BehaviorDefinition() {
    override fun toBlueprint() = FaceTargetBlueprint()
}

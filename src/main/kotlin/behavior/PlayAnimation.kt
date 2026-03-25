package com.shadowforgedmmo.engine.behavior

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.NonPlayerCharacter

class PlayAnimation(private val animation: String) : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        character.playAnimation(animation)
        return BehaviorStatus.SUCCESS
    }
}

class PlayAnimationBlueprint(
    private val animation: String
) : BehaviorBlueprint() {
    override fun create(): Behavior = PlayAnimation(animation)
}

data class PlayAnimationDefinition(
    @JsonProperty("animation") val animation: String
) : BehaviorDefinition() {
    override fun toBlueprint() =
        PlayAnimationBlueprint(animation)
}

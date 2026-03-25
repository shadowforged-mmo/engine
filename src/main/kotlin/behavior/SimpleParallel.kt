package com.shadowforgedmmo.engine.behavior

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.NonPlayerCharacter

class SimpleParallel(
    primary: Behavior,
    secondary: Behavior
) : Composite(listOf(primary, secondary)) {
    private val primary
        get() = children[0]

    private val secondary
        get() = children[1]

    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val status = primary.tick(character)
        secondary.tick(character)
        return status
    }
}

class SimpleParallelBlueprint(
    private val primary: BehaviorBlueprint,
    private val secondary: BehaviorBlueprint
) : BehaviorBlueprint() {
    override fun create() = SimpleParallel(primary.create(), secondary.create())
}

data class SimpleParallelDefinition(
    @JsonProperty("primary") val primary: BehaviorDefinition,
    @JsonProperty("secondary") val secondary: BehaviorDefinition
) : BehaviorDefinition() {
    override fun toBlueprint() =
        SimpleParallelBlueprint(primary.toBlueprint(), secondary.toBlueprint())
}

package com.shadowforgedmmo.engine.behavior

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import com.shadowforgedmmo.engine.math.weightedRandomIndex

class RandomSelector(
    private val weights: List<Double>,
    children: List<Behavior>
) : Composite(children) {
    private var currentChild = 0

    init {
        require(weights.all { it >= 0.0 })
        require(children.size == weights.size)
    }

    override fun start(character: NonPlayerCharacter) {
        currentChild = weightedRandomIndex(weights)
    }

    override fun update(character: NonPlayerCharacter) =
        children[currentChild].tick(character)
}

class RandomSelectorBlueprint(
    private val weights: List<Double>,
    private val children: List<BehaviorBlueprint>
) : BehaviorBlueprint() {
    override fun create() = RandomSelector(
        weights,
        children.map(BehaviorBlueprint::create)
    )
}

data class RandomSelectorDefinition(
    @JsonProperty("weights") val weights: List<Double>,
    @JsonProperty("children") val children: List<BehaviorDefinition>
) : BehaviorDefinition() {
    override fun toBlueprint() =
        RandomSelectorBlueprint(weights, children.map(BehaviorDefinition::toBlueprint))
}

package com.shadowforgedmmo.engine.character

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.gameobject.GameObjectSpawner
import com.shadowforgedmmo.engine.instance.Instance
import com.shadowforgedmmo.engine.math.Position
import com.shadowforgedmmo.engine.resource.Registry
import com.shadowforgedmmo.engine.runtime.Runtime

class NonPlayerCharacterSpawner(
    position: Position,
    val blueprint: CharacterBlueprint,
    val summoner: Character? = null
) : GameObjectSpawner(position) {
    override fun start(instance: Instance) =
        blueprint.interactions.forEach { it.start(instance, position) }

    override fun spawn(instance: Instance, runtime: Runtime) =
        NonPlayerCharacter(this, instance, runtime)
}

data class CharacterSpawnsDefinition(
    @JsonProperty("character") val characterBlueprintReference: CharacterBlueprintReference,
    @JsonProperty("positions") val positions: List<Position> // TODO: handle single position
) {
    fun toCharacterSpawners(
        characterBlueprintRegistry: Registry<CharacterBlueprint>
    ): Collection<NonPlayerCharacterSpawner> {
        val blueprint = characterBlueprintReference.resolve(characterBlueprintRegistry)
        return positions.map {
            NonPlayerCharacterSpawner(it, blueprint)
        }
    }
}

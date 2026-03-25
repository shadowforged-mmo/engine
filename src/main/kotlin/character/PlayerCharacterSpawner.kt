package com.shadowforgedmmo.engine.character

import com.shadowforgedmmo.engine.gameobject.GameObjectSpawner
import com.shadowforgedmmo.engine.instance.Instance
import com.shadowforgedmmo.engine.runtime.Runtime
import net.minestom.server.entity.Player

class PlayerCharacterSpawner(
    val entity: Player,
    val playerCharacterDefinition: PlayerCharacterDefinition
) : GameObjectSpawner(playerCharacterDefinition.position) {
    override fun spawn(instance: Instance, runtime: Runtime) =
        playerCharacterDefinition.toPlayerCharacter(this, runtime, entity)
}

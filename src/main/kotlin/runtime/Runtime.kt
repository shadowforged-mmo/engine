package com.shadowforgedmmo.engine.runtime

import com.shadowforgedmmo.engine.character.CharacterBlueprint
import com.shadowforgedmmo.engine.character.CharacterEvents
import com.shadowforgedmmo.engine.instance.Instance
import com.shadowforgedmmo.engine.item.Item
import com.shadowforgedmmo.engine.login.LoginManager
import com.shadowforgedmmo.engine.music.Song
import com.shadowforgedmmo.engine.playerclass.PlayerClass
import com.shadowforgedmmo.engine.quest.Quest
import com.shadowforgedmmo.engine.quest.QuestObjectiveManager
import com.shadowforgedmmo.engine.resource.Resources
import com.shadowforgedmmo.engine.script.Interpreter
import com.shadowforgedmmo.engine.skill.Skill
import com.shadowforgedmmo.engine.util.schedulerManager
import com.shadowforgedmmo.engine.zone.Zone
import net.minestom.server.MinecraftServer
import net.minestom.server.timer.TaskSchedule

class Runtime(resources: Resources) {
    private val server = resources.server
    val interpreter = Interpreter(resources.scriptDir)
    val config = resources.config
    val playerClassesById = resources.playerClasses.associateBy(PlayerClass::id)
    val skillsById = resources.skills.associateBy(Skill::id)
    val itemsById = resources.items.associateBy(Item::id)
    val instancesById = resources.instances.associateBy(Instance::id)
    val questsById = resources.quests.associateBy(Quest::id)
    val musicById = resources.music.associateBy(Song::id)
    val characterBlueprintsById = resources.characterBlueprints.associateBy(CharacterBlueprint::id)
    val zonesById = resources.zones.associateBy(Zone::id)
    val questObjectiveManager = QuestObjectiveManager()
    val loginManager = LoginManager(this)
    val characterEvents = CharacterEvents(this)

    var timeMillis = 0L
        private set

    fun start() {
        interpreter.loadScriptLibrary(this)
        instancesById.values.forEach(Instance::start)
        questsById.values.forEach { it.start(this) }
        loginManager.start()
        characterEvents.start()

        schedulerManager.buildTask(::tick)
            .repeat(TaskSchedule.tick(1))
            .schedule()

        server.start("0.0.0.0", 25565)
    }

    private fun tick() {
        instancesById.values.forEach { it.tick(this) }

        timeMillis += MinecraftServer.TICK_MS
    }
}

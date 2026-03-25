package com.shadowforgedmmo.engine.runtime

import com.shadowforgedmmo.engine.api.ApiClient
import com.shadowforgedmmo.engine.instance.Instance
import com.shadowforgedmmo.engine.login.LoginManager
import com.shadowforgedmmo.engine.quest.QuestObjectiveManager
import com.shadowforgedmmo.engine.resource.Resources
import com.shadowforgedmmo.engine.script.Interpreter
import com.shadowforgedmmo.engine.util.schedulerManager
import net.minestom.server.MinecraftServer
import net.minestom.server.timer.TaskSchedule

class Runtime(val resources: Resources) {
    val interpreter = Interpreter(resources.scriptDir)
    val apiClient = ApiClient(this)
    val questObjectiveManager = QuestObjectiveManager()
    val loginManager = LoginManager(this)

    var timeMillis = 0L
        private set

    fun start() {
        interpreter.loadScriptLibrary(this)
        resources.instanceRegistry.values.forEach(Instance::start)
        resources.questRegistry.values.forEach { it.start(this) }
        loginManager.start()

        schedulerManager.buildTask(::tick)
            .repeat(TaskSchedule.tick(1))
            .schedule()

        resources.server.start("0.0.0.0", 25565)
    }

    private fun tick() {
        resources.instanceRegistry.values.forEach { it.tick(this) }

        timeMillis += MinecraftServer.TICK_MS
    }

    fun stop() {
        interpreter.close()
    }
}

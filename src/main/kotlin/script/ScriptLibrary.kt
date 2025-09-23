@file:Suppress("FunctionName", "PropertyName", "unused")

package com.shadowforgedmmo.engine.script

import com.shadowforgedmmo.engine.combat.DamageType
import com.shadowforgedmmo.engine.math.BoundingBox3
import com.shadowforgedmmo.engine.runtime.Runtime
import com.shadowforgedmmo.engine.skill.SkillStatus
import com.shadowforgedmmo.engine.time.millisToSeconds
import com.shadowforgedmmo.engine.time.secondsToDuration
import com.shadowforgedmmo.engine.util.schedulerManager
import net.minestom.server.particle.Particle
import org.python.core.Py
import org.python.core.PyBoolean
import org.python.core.PyModule
import org.python.core.PyObject
import org.python.core.PyStringMap
import com.shadowforgedmmo.engine.character.Character as EngineCharacter
import com.shadowforgedmmo.engine.character.NonPlayerCharacter as EngineNonPlayerCharacter
import com.shadowforgedmmo.engine.character.PlayerCharacter as EnginePlayerCharacter
import com.shadowforgedmmo.engine.instance.Instance as EngineInstance
import com.shadowforgedmmo.engine.skill.SkillExecutor as EngineSkillExecutor
import net.minestom.server.timer.Task as EngineTask

const val SCRIPT_LIBRARY_MODULE_NAME = "shadowforged_engine"

fun scriptLibraryModule(runtime: Runtime): PyModule {
    val classes = listOf(
        Point::class,
        Vector::class,
        Position::class,
        Instance::class,
        NonPlayerCharacter::class,
        SkillExecutor::class,
        SkillStatus::class,
        Damage::class,
        DamageType::class,
        Sound::class
    ).associate { it.simpleName to Py.java2py(it.java) }

    val functions = mapOf(
        "get_time" to GetTime(runtime),
        "run_delayed" to RunDelayed(runtime)
    )

    return PyModule(SCRIPT_LIBRARY_MODULE_NAME, PyStringMap(classes + functions))
}

interface Point {
    val x: Double
    val y: Double
    val z: Double
}

data class Vector(
    override val x: Double,
    override val y: Double,
    override val z: Double
) : Point {
    companion object {
        @JvmField
        val ZERO = Vector(0.0, 0.0, 0.0)

        @JvmField
        val ONE = Vector(1.0, 1.0, 1.0)

        @JvmField
        val LEFT = Vector(1.0, 0.0, 0.0)

        @JvmField
        val RIGHT = Vector(-1.0, 0.0, 0.0)

        @JvmField
        val UP = Vector(0.0, 1.0, 0.0)

        @JvmField
        val DOWN = Vector(0.0, -1.0, 0.0)

        @JvmField
        val FORWARD = Vector(0.0, 0.0, 1.0)

        @JvmField
        val BACK = Vector(0.0, 0.0, -1.0)
    }

    fun __add__(v: Vector) = Vector(x + v.x, y + v.y, z + v.z)

    fun __sub__(v: Vector) = Vector(x - v.x, y - v.y, z - v.z)

    fun __mul__(s: Double) = Vector(x * s, y * s, z * s)
}

data class Position(
    override val x: Double,
    override val y: Double,
    override val z: Double,
    val yaw: Double,
    val pitch: Double
) : Point {
    val direction: Vector
        get() = EngineToScript.vector3(ScriptToEngine.position(this).direction)

    fun __add__(v: Vector) = Position(x + v.x, y + v.y, z + v.z, yaw, pitch)

    fun __sub__(v: Vector) = Position(x - v.x, y - v.y, z - v.z, yaw, pitch)
}

class Instance(val handle: EngineInstance) {
    val id: String
        get() = handle.id

    fun spawn_character(position: Point, character: String) = Unit

    fun get_characters_in_box(center: Point, half_extents: Vector, filter: PyObject) =
        handle.getObjectsInBox<EngineCharacter>(
            BoundingBox3.from(
                ScriptToEngine.vector3(center),
                ScriptToEngine.vector3(half_extents)
            )
        )
            .filter { (filter.__call__(Py.java2py(it.handle)) as PyBoolean).booleanValue }
            .map(EngineCharacter::handle)

    fun play_sound(position: Point, sound: Sound) = handle.playSound(
        ScriptToEngine.vector3(position),
        ScriptToEngine.sound(sound)
    )

    fun spawn_particle(position: Point, particle: String) = handle.spawnParticle(
        ScriptToEngine.vector3(position),
        Particle.fromKey(particle) ?: throw IllegalArgumentException()
    )
}

class Task(private val handle: EngineTask) {
    fun cancel() = handle.cancel()
}

data class Damage(val damage: Map<DamageType, Double>) {
    constructor(amount: Double) : this(mapOf(DamageType.PHYSICAL to amount))
}

data class Sound(val name: String, val volume: Float, val pitch: Float) {
    constructor(name: String) : this(name, 1.0F, 1.0F)
}

open class Character(
    private val handle: EngineCharacter
) {
    fun getIs_on_ground() = handle.isOnGround

    val instance
        get() = handle.instance.handle

    val position
        get() = EngineToScript.position(handle.position)

    var velocity
        get() = EngineToScript.vector3(handle.velocity)
        set(value) {
            handle.velocity = ScriptToEngine.vector3(value)
        }

    fun damage(damage: Damage, source: Character) = handle.damage(
        ScriptToEngine.damage(damage),
        source.handle
    )
}

class PlayerCharacter(
    private val handle: EnginePlayerCharacter
) : Character(handle) {
}

open class NonPlayerCharacter(
    private val handle: EngineNonPlayerCharacter
) : Character(handle) {
    open fun tick() = Unit

    open fun on_spawn() = Unit

    open fun on_despawn() = Unit
}

open class SkillExecutor(private val handle: EngineSkillExecutor) {
    val user
        get() = handle.user.handle

    val lifetime
        get() = millisToSeconds(handle.lifetimeMillis)

    fun complete() = handle.complete()

    open fun init() = Unit

    open fun tick() = Unit
}

class GetTime(val runtime: Runtime) : PyObject() {
    override fun __call__(): PyObject = Py.java2py(millisToSeconds(runtime.timeMillis))
}

class RunDelayed(val runtime: Runtime) : PyObject() {
    override fun __call__(args: Array<PyObject>, keywords: Array<String>): PyObject {
        val delay = args[0].asDouble()
        val function = args[1]
        return Py.java2py(
            Task(
                schedulerManager.buildTask(function::__call__).delay(secondsToDuration(delay)).schedule()
            )
        )
    }
}

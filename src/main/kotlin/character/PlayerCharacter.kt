package com.shadowforgedmmo.engine.character

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.combat.Damage
import com.shadowforgedmmo.engine.combat.DamageType
import com.shadowforgedmmo.engine.entity.Hologram
import com.shadowforgedmmo.engine.instance.Instance
import com.shadowforgedmmo.engine.instance.InstanceReference
import com.shadowforgedmmo.engine.item.Inventory
import com.shadowforgedmmo.engine.item.InventoryDefinition
import com.shadowforgedmmo.engine.math.BoundingBox3
import com.shadowforgedmmo.engine.math.Position
import com.shadowforgedmmo.engine.math.Vector3
import com.shadowforgedmmo.engine.music.MusicPlayer
import com.shadowforgedmmo.engine.playerclass.PlayerClass
import com.shadowforgedmmo.engine.playerclass.PlayerClassReference
import com.shadowforgedmmo.engine.quest.QuestProgress
import com.shadowforgedmmo.engine.quest.QuestTracker
import com.shadowforgedmmo.engine.resource.Resources
import com.shadowforgedmmo.engine.runtime.Runtime
import com.shadowforgedmmo.engine.skill.SkillTracker
import com.shadowforgedmmo.engine.time.secondsToTicks
import com.shadowforgedmmo.engine.util.loadJsonResource
import com.shadowforgedmmo.engine.util.schedulerManager
import com.shadowforgedmmo.engine.util.toMinestom
import com.shadowforgedmmo.engine.zone.Zone
import com.shadowforgedmmo.engine.zone.ZoneReference
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerEntityInteractEvent
import net.minestom.server.event.player.PlayerHandAnimationEvent
import net.minestom.server.network.packet.server.play.HitAnimationPacket
import net.minestom.server.potion.Potion
import net.minestom.server.potion.PotionEffect
import net.minestom.server.timer.TaskSchedule
import java.time.Duration
import kotlin.math.roundToInt
import com.shadowforgedmmo.engine.script.PlayerCharacter as ScriptPlayerCharacter

val experiencePointsPerLevel = loadJsonResource(
    "data/experience_points_per_level.json",
    Array<Int>::class
).toList() + listOf(Integer.MAX_VALUE)
val experiencePointsPerLevelPrefixSum =
    listOf(0) + experiencePointsPerLevel.dropLast(1).runningReduce { acc, points -> acc + points }
val maxExperiencePoints = experiencePointsPerLevelPrefixSum.last()

class PlayerCharacter(
    spawner: PlayerCharacterSpawner,
    instance: Instance,
    runtime: Runtime,
    override val entity: Player,
    maxHealth: Int,
    health: Int,
    maxMana: Int,
    mana: Int,
    val playerClass: PlayerClass,
    val skillTracker: SkillTracker,
    val questTracker: QuestTracker,
    val inventory: Inventory,
    val musicPlayer: MusicPlayer,
    zone: Zone
) : Character(spawner, instance, runtime) {
    override val name
        get() = entity.username

    override var level = 1 // TODO INITIALIZE USING DATA
        private set

    var experiencePoints = 0 // TODO INITIALIZE USING DATA
        private set

    override var maxHealth = maxHealth
        set(value) {
            field = value
            updateHealthBar()
            nameplate.updateHealthBar()
        }

    override var health = health
        set(value) {
            field = value
            updateHealthBar()
            nameplate.updateHealthBar()
        }

    override val mass
        get() = 75.0

    var maxMana = maxMana
        set(value) {
            field = value
            updateManaBar()
        }

    var mana = mana
        set(value) {
            field = value
            updateManaBar()
        }

    override val handle = ScriptPlayerCharacter(this)

    var bossFights = mutableSetOf<BossFight>()

    var zone = zone
        set(value) {
            field = value
            enterZone()
        }

    override val removeEntityOnDespawn
        get() = false

    override fun spawn() {
        // entityTeleporting = true
        super.spawn()
        addEventListeners()
        updateExperienceBar()
        updateLevelDisplay()
        questTracker.start()
        entity.setHeldItemSlot(8)
        enterZone()
    }

    private fun addEventListeners() {
        with(entity.eventNode()) {
            addListener(PlayerChangeHeldSlotEvent::class.java, ::handleChangeHeldSlot)
            addListener(PlayerEntityInteractEvent::class.java, ::handleEntityInteract)
            addListener(PlayerHandAnimationEvent::class.java, ::handleHandAnimation)
            addListener(PlayerDisconnectEvent::class.java, ::handleDisconnect)
        }
    }

    override fun despawn() {
        super.despawn()
        // TODO: save data
    }

    override fun tick() {
        super.tick()
        skillTracker.tick()
        entity.sendActionBar(actionBar())
        updateZone()
    }

    private fun updateZone() {
        val newZone = instance.zoneAt(position.toVector2())
        if (newZone != null &&
            newZone != zone &&
            (!zone.outerBoundary.contains(position.toVector2()) ||
                    newZone.type.priority > zone.type.priority)
        ) {
            zone = newZone
        }
    }

    override fun getStance(toward: Character) =
        if (toward is PlayerCharacter)
            Stance.FRIENDLY
        else
            toward.getStance(this)

    override fun damage(damage: Damage, source: Character) {
        super.damage(damage, source)
        entity.sendPacketToViewersAndSelf(
            HitAnimationPacket(entity.entityId, position.yaw.toFloat())
        )
    }

    override fun die() {
        disableMovement()

        entity.showTitle(
            Title.title(
                Component.text("YOU DIED", NamedTextColor.RED),
                Component.empty()
            )
        )

        entity.heal()

        entity.addEffect(Potion(PotionEffect.BLINDNESS, 1, secondsToTicks(4.0)))

        schedulerManager.buildTask(::respawn)
            .delay(Duration.ofSeconds(3))
            .schedule()
    }

    private fun respawn() {
        health = maxHealth
        mana = maxMana
        val respawnPosition = position // TODO
        entity.teleport(respawnPosition.toMinestom())
        enableMovement()
    }

    fun addExperiencePoints(points: Int, position: Vector3) {
        experiencePoints = (experiencePoints + points).coerceAtMost(maxExperiencePoints)
        checkForLevelUp()
        updateExperienceBar()
        spawnExperiencePointsNotification(points, position)
    }

    private fun checkForLevelUp() {
        while (experiencePointsIntoLevel() >= experiencePointsForNextLevel()) levelUp()
    }

    private fun levelUp() {
        level++
        updateLevelDisplay()
        // TODO: sounds, particles, and message
    }

    private fun updateLevelDisplay() {
        entity.level = level
    }

    private fun updateExperienceBar() {
        entity.exp = experiencePointsIntoLevel().toFloat() / experiencePointsForNextLevel().toFloat()
    }

    private fun experiencePointsIntoLevel() = experiencePoints - experiencePointsPerLevelPrefixSum[level - 1]

    private fun experiencePointsForNextLevel() = experiencePointsPerLevel[level - 1]

    private fun spawnExperiencePointsNotification(points: Int, position: Vector3) {
        val hologram = Hologram()
        hologram.text = Component.text("+$points XP", NamedTextColor.GREEN)
        hologram.isAutoViewable = false
        hologram.velocity = (Vector3.UP * 0.5).toMinestom()
        hologram.setInstance(instance.instanceContainer, position.toMinestom()).join()
        hologram.addViewer(entity)
        schedulerManager.buildTask(hologram::remove).delay(TaskSchedule.seconds(2)).schedule()
    }

    fun sendMessage(message: Component) = entity.sendMessage(message)

    private fun updateHealthBar() {
        entity.health = (20.0 * health / maxHealth).toFloat().coerceAtLeast(1.0F)
    }

    private fun updateManaBar() {
        entity.food = (20.0 * mana / maxMana).roundToInt()
    }

    private fun actionBar() =
        Component.text(
            "❤ $health/$maxHealth",
            NamedTextColor.RED
        ).append(
            Component.text(
                "     "
            )
        ).append(
            Component.text(
                "❈ $mana/$maxMana",
                NamedTextColor.AQUA
            )
        )

    private fun enterZone() {
        val title = Title.title(zone.displayName, zone.levelText)
        entity.showTitle(title)
        if (bossFights.isEmpty()) musicPlayer.setSong(zone.music)
    }

    fun updateMusic() {
        val song = bossFights.maxWithOrNull(
            compareBy<BossFight> {
                it.character.level
            }.thenBy {
                it.character.name
            }
        )?.music ?: zone.music
        musicPlayer.setSong(song)
    }

    override fun interact(pc: PlayerCharacter) = Unit

    fun playSound(sound: Sound) = entity.playSound(sound)

    fun playSound(sound: Sound, from: Vector3) =
        entity.playSound(sound, from.toMinestom())

    fun disableMovement() {
        entity.getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.0
    }

    fun enableMovement() {
        entity.getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.1
    }

    private fun handleChangeHeldSlot(event: PlayerChangeHeldSlotEvent) {
        val slot = event.newSlot.toInt()
        event.isCancelled = true
        if (slot < 6) {
            skillTracker.tryUseSkill(slot)
        } else if (slot < 8) {
            inventory.tryUseConsumable(slot)
        }
    }

    private fun handleEntityInteract(event: PlayerEntityInteractEvent) {
        if (event.hand != PlayerHand.MAIN) return
        fromEntity(event.target)?.interact(this)
    }

    private fun handleHandAnimation(event: PlayerHandAnimationEvent) {
        if (event.hand == PlayerHand.MAIN) autoAttack()
    }

    private fun autoAttack() {
        val hits = instance.getObjectsInBox<Character>(
            BoundingBox3.from(
                eyePosition + position.direction * 3.0,
                Vector3.ONE
            )
        ).filter { it != this }
        if (hits.isEmpty()) {

        } else {
            val damage = Damage(mapOf(DamageType.PHYSICAL to 1))
            hits.forEach {
                it.damage(damage, this)
            }
        }
    }

    private fun handleDisconnect(event: PlayerDisconnectEvent) {
        remove()
    }
}

class PlayerCharacterDefinition(
    @JsonProperty("class") val playerClassReference: PlayerClassReference,
    @JsonProperty("instance") val instanceReference: InstanceReference,
    @JsonProperty("position") val position: Position,
    @JsonProperty("zone") val zoneReference: ZoneReference,
    @JsonProperty("maxHealth") val maxHealth: Int,
    @JsonProperty("health") val health: Int,
    @JsonProperty("maxMana") val maxMana: Int,
    @JsonProperty("mana") val mana: Int,
    @JsonProperty("quests") val questProgress: QuestProgress,
    @JsonProperty("inventory") val inventory: InventoryDefinition
) {
    fun toPlayerCharacter(spawner: PlayerCharacterSpawner, runtime: Runtime, entity: Player) =
        with(runtime.resources) {
            PlayerCharacter(
                spawner,
                instanceReference.resolve(instanceRegistry),
                runtime,
                entity,
                maxHealth,
                health,
                maxMana,
                mana,
                playerClassReference.resolve(classRegistry),
                SkillTracker(TODO()),
                QuestTracker(TODO(), questProgress),
                inventory.toInventory(entity.inventory, itemRegistry),
                MusicPlayer(TODO()),
                zoneReference.resolve(zoneRegistry)
            )
        }
}

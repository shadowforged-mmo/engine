package com.shadowforgedmmo.engine.character

import com.fasterxml.jackson.annotation.JsonProperty
import com.shadowforgedmmo.engine.combat.Damage
import com.shadowforgedmmo.engine.combat.DamageType
import com.shadowforgedmmo.engine.entity.Hologram
import com.shadowforgedmmo.engine.instance.Instance
import com.shadowforgedmmo.engine.instance.InstanceReference
import com.shadowforgedmmo.engine.item.*
import com.shadowforgedmmo.engine.math.BoundingBox3
import com.shadowforgedmmo.engine.math.Position
import com.shadowforgedmmo.engine.math.Vector3
import com.shadowforgedmmo.engine.music.MusicTrack
import com.shadowforgedmmo.engine.pack.Namespaces
import com.shadowforgedmmo.engine.playerclass.PlayerClass
import com.shadowforgedmmo.engine.playerclass.PlayerClassReference
import com.shadowforgedmmo.engine.quest.Quest
import com.shadowforgedmmo.engine.quest.QuestObjective
import com.shadowforgedmmo.engine.quest.QuestProgress
import com.shadowforgedmmo.engine.quest.QuestProgressDefinition
import com.shadowforgedmmo.engine.runtime.Runtime
import com.shadowforgedmmo.engine.skill.ActiveSkill
import com.shadowforgedmmo.engine.skill.SKILL_TAG
import com.shadowforgedmmo.engine.skill.SkillExecutor
import com.shadowforgedmmo.engine.skill.SkillReference
import com.shadowforgedmmo.engine.time.Cooldown
import com.shadowforgedmmo.engine.time.secondsToTicks
import com.shadowforgedmmo.engine.util.loadJsonResource
import com.shadowforgedmmo.engine.util.schedulerManager
import com.shadowforgedmmo.engine.util.toMinestom
import com.shadowforgedmmo.engine.zone.Zone
import com.shadowforgedmmo.engine.zone.ZoneReference
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerEntityInteractEvent
import net.minestom.server.event.player.PlayerHandAnimationEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.server.play.HitAnimationPacket
import net.minestom.server.potion.Potion
import net.minestom.server.potion.PotionEffect
import net.minestom.server.scoreboard.Sidebar
import net.minestom.server.scoreboard.Sidebar.ScoreboardLine
import net.minestom.server.timer.Task
import net.minestom.server.timer.TaskSchedule
import net.minestom.server.utils.inventory.PlayerInventoryUtils
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
    questProgress: QuestProgress,
    private val inventory: Inventory,
    private val actionBarSkills: Array<ActiveSkill?>,
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

    private val skillCooldowns = mutableMapOf<ActiveSkill, Cooldown>()
    private val skillExecutors = mutableListOf<SkillExecutor>()

    private val completedQuestIds = questProgress.completedQuestIds.toMutableSet()
    private val trackedQuests = questProgress.tracked.toMutableSet()
    private val questObjectives = questProgress.objectivesByQuestId.toMutableMap()

    private var musicTrack: MusicTrack? = null
    private var musicReplayTask: Task? = null

    override fun spawn() {
        // entityTeleporting = true
        super.spawn()
        addEventListeners()
        updateExperienceBar()
        updateLevelDisplay()
        updateSidebar()
        initInventory()
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
        tickSkills()
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
        if (bossFights.isEmpty()) setMusicTrack(zone.music)
    }

    fun updateMusicTrack() {
        val musicTrack = bossFights.maxWithOrNull(
            compareBy<BossFight> {
                it.character.level
            }.thenBy {
                it.character.name
            }
        )?.music ?: zone.music
        setMusicTrack(musicTrack)
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
            tryUseSkill(slot)
        } else if (slot < 8) {
            tryUseConsumable(slot)
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

    fun tryUseSkill(slot: Int) {
        val skill = hotbarSkill(slot) ?: return
        tryUseSkill(skill)
    }

    private fun hotbarSkill(slot: Int) = entity.inventory.getItemStack(slot).getTag(SKILL_TAG)?.let {
        runtime.resources.skillRegistry.getValue(it) as? ActiveSkill
    }

    private fun tryUseSkill(skill: ActiveSkill) {
        val cooldown = cooldown(skill)
        if (cooldown.hasCooldown(runtime.timeMillis))
            return failUseSkill(Component.text("On cooldown")) // TODO

        if (mana < skill.manaCost)
            return failUseSkill(Component.text("Not enough mana")) // TODO

        useSkill(skill)
    }

    private fun failUseSkill(message: Component) {
        sendMessage(message)
        // TODO: play sound
    }

    private fun useSkill(skill: ActiveSkill) {
        val skillExecutor = SkillExecutor(this, skill, runtime.timeMillis)
        skillExecutor.init()
        skillExecutor.tick()
        if (!skillExecutor.completed) {
            skillExecutors.add(skillExecutor)
        }
        val cooldown = Cooldown(skill.cooldownMillis)
        cooldown.set(runtime.timeMillis)
        skillCooldowns[skill] = cooldown
        mana -= skill.manaCost
    }

    private fun tickSkills() {
        skillCooldowns.values.removeIf { !it.hasCooldown(runtime.timeMillis) }
        skillExecutors.forEach(SkillExecutor::tick)
        skillExecutors.removeIf(SkillExecutor::completed)
        updateHotbar()
    }

    private fun updateHotbar() = (0..5).forEach(::updateHotbarSlot)

    private fun updateHotbarSlot(slot: Int) {
        entity.inventory.setItemStack(0, (playerClass.skills[0] as ActiveSkill).hotbarItemStack(this))
        entity.inventory.setItemStack(1, (playerClass.skills[1] as ActiveSkill).hotbarItemStack(this))
        val skill = hotbarSkill(slot)
        if (skill == null) {
            entity.inventory.setItemStack(slot, ItemStack.of(Material.BARRIER))
            return
        }
        entity.inventory.setItemStack(slot, skill.hotbarItemStack(this))
    }

    fun cooldown(skill: ActiveSkill) = skillCooldowns[skill] ?: Cooldown(skill.cooldownMillis)

    fun startQuest(quest: Quest) {
        if (quest.id in questObjectives) error("Quest already started")

        trackedQuests += quest
        questObjectives[quest.id] = IntArray(quest.objectives.size)

        entity.showTitle(
            Title.title(
                Component.text("Quest Started", NamedTextColor.YELLOW),
                Component.text(quest.name, NamedTextColor.YELLOW)
            )
        )

        // TODO: clean up
        playSound(
            Sound.sound(
                Key.key(Namespaces.ENGINE_SOUNDS, "quest_start"),
                Sound.Source.MASTER,
                1.0F,
                1.0F
            )
        )

        updateSidebar()
    }

    fun completeQuest(quest: Quest) {
        trackedQuests -= quest
        questObjectives -= quest.id

        entity.showTitle(
            Title.title(
                Component.text("Quest Completed", NamedTextColor.YELLOW),
                Component.text(quest.name, NamedTextColor.YELLOW)
            )
        )

        playSound(
            Sound.sound(
                Key.key(Namespaces.ENGINE_SOUNDS, "quest_complete"),
                Sound.Source.MASTER,
                1.0F,
                1.0F
            )
        )

        updateSidebar()
    }

    fun isInProgress(quest: Quest) = quest.id in questObjectives

    fun isInProgress(quest: Quest, objectiveIndex: Int) = isInProgress(quest) &&
            getProgress(quest, objectiveIndex) < quest.objectives[objectiveIndex].goal

    fun getProgress(quest: Quest, objectiveIndex: Int) =
        questObjectives.getValue(quest.id)[objectiveIndex]

    fun addProgress(quest: Quest, objectiveIndex: Int, progress: Int) {
        if (quest.id !in questObjectives) return
        val oldProgress = questObjectives.getValue(quest.id)[objectiveIndex]
        val newProgress = (oldProgress + progress)
            .coerceAtMost(quest.objectives[objectiveIndex].goal)
        if (oldProgress == newProgress) return
        questObjectives.getValue(quest.id)[objectiveIndex] = newProgress
        updateSidebar()
    }

    fun incrementProgress(quest: Quest, objectiveIndex: Int) = addProgress(quest, objectiveIndex, 1)

    fun isReadyToStart(quest: Quest) = level >= quest.level &&
            quest.id !in completedQuestIds &&
            quest.id !in questObjectives &&
            quest.prerequisiteReferences.all { it.id in completedQuestIds }

    fun isReadyToTurnIn(quest: Quest) = quest.id in questObjectives &&
            quest.objectives.withIndex().all { (index, objective) ->
                getProgress(quest, index) == objective.goal
            }

    private fun updateSidebar() {
        sidebar().addViewer(entity)
    }

    private fun initInventory() {
        setInventoryItem(8, inventory.weapon)
        setInventoryItem(PlayerInventoryUtils.OFFHAND_SLOT, inventory.offhand)
        setInventoryItem(PlayerInventoryUtils.BOOTS_SLOT, inventory.feet)
        setInventoryItem(PlayerInventoryUtils.LEGGINGS_SLOT, inventory.legs)
        setInventoryItem(PlayerInventoryUtils.CHESTPLATE_SLOT, inventory.chest)
        setInventoryItem(PlayerInventoryUtils.HELMET_SLOT, inventory.head)
        setInventoryItem(9, inventory.finger1)
        setInventoryItem(10, inventory.finger2)
        setInventoryItem(11, inventory.wrist)
        setInventoryItem(12, inventory.trinket)
        inventory.bag.forEachIndexed { index, item -> setInventoryItem(13 + index, item) }
    }

    private fun setInventoryItem(slot: Int, itemInstance: ItemInstance?) {
        entity.inventory.setItemStack(slot, itemInstance?.itemStack(this) ?: ItemStack.AIR)
    }

    private fun sidebar(): Sidebar {
        val sidebar = Sidebar(Component.text("Quests", NamedTextColor.YELLOW))
        val sidebarContent = sidebarContent()
        val numLines = minOf(15, sidebarContent.size)
        for (i in 0..<numLines) {
            val lineId = i.toString()
            val line = numLines - i - 1
            sidebar.createLine(ScoreboardLine(lineId, sidebarContent[i], line))
        }
        return sidebar
    }

    private fun sidebarContent(): List<Component> =
        trackedQuests.flatMapIndexed(::sidebarQuestContent)

    private fun sidebarQuestContent(index: Int, quest: Quest) = listOf(
        Component.text("(", NamedTextColor.YELLOW)
            .append(Component.text("${index + 1}").decorate(TextDecoration.BOLD))
            .append(Component.text(") ${quest.name}")),
        *quest.objectives.mapIndexed { objectiveIndex, objective ->
            sidebarObjectiveContent(quest, objectiveIndex, objective)
        }.toTypedArray()
    )

    private fun sidebarObjectiveContent(
        quest: Quest,
        objectiveIndex: Int,
        objective: QuestObjective
    ) = Component.empty().append(
        Component.text(
            " • ${getProgress(quest, objectiveIndex)}/${objective.goal} ",
            NamedTextColor.YELLOW,
            TextDecoration.BOLD
        )
    ).append(Component.text(objective.description, NamedTextColor.WHITE))

    val weapon: WeaponInstance?
        get() = equipment(8) as? WeaponInstance

    val offhand: EquipmentItemInstance?
        get() = equipment(PlayerInventoryUtils.OFFHAND_SLOT)

    val feet: ArmorItemInstance?
        get() = equipment(PlayerInventoryUtils.BOOTS_SLOT) as? ArmorItemInstance

    val legs: ArmorItemInstance?
        get() = equipment(PlayerInventoryUtils.LEGGINGS_SLOT) as? ArmorItemInstance

    val chest: ArmorItemInstance?
        get() = equipment(PlayerInventoryUtils.CHESTPLATE_SLOT) as? ArmorItemInstance

    val head: ArmorItemInstance?
        get() = equipment(PlayerInventoryUtils.HELMET_SLOT) as? ArmorItemInstance

    val finger1: AccessoryInstance?
        get() = equipment(9) as? AccessoryInstance

    val finger2: AccessoryInstance?
        get() = equipment(10) as? AccessoryInstance

    val wrist: AccessoryInstance?
        get() = equipment(11) as? AccessoryInstance

    val trinket: AccessoryInstance?
        get() = equipment(12) as? AccessoryInstance

    fun equipment(inventorySlot: Int): EquipmentItemInstance? {
        val itemStack = entity.inventory.getItemStack(inventorySlot)
        val itemId = itemStack.getTag(ITEM_ID_TAG) ?: return null
        val itemRegistry = runtime.resources.itemRegistry
        val equipment = itemRegistry[itemId] as EquipmentItem
        val gems = (0..<equipment.sockets).mapNotNull {
            itemStack.getTag(socketTag(it))?.let(itemRegistry::getValue) as? Gem
        }
        return equipment.instance(gems)
    }

    fun tryUseConsumable(slot: Int) {

    }

    fun setMusicTrack(musicTrack: MusicTrack?) {
        if (musicTrack == this.musicTrack) return

        musicReplayTask?.cancel()
        this.musicTrack?.let { entity.stopSound(it.sound) }

        musicTrack?.let {
            musicReplayTask = schedulerManager.buildTask {
                entity.playSound(it.sound)
            }.repeat(it.duration).schedule()
        }
        this.musicTrack = musicTrack
    }
}

class PlayerCharacterDefinition(
    @JsonProperty("class") val playerClassReference: PlayerClassReference,
    @JsonProperty("instance") val instanceReference: InstanceReference,
    @JsonProperty("position") val position: Position,
    @JsonProperty("zone") val zoneReference: ZoneReference,
    @JsonProperty("max_health") val maxHealth: Int,
    @JsonProperty("health") val health: Int,
    @JsonProperty("max_mana") val maxMana: Int,
    @JsonProperty("mana") val mana: Int,
    @JsonProperty("quests") val questProgress: QuestProgressDefinition,
    @JsonProperty("action_bar_skills") val actionBarSkills: List<SkillReference?>,
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
                questProgress.toQuestProgress(questRegistry),
                inventory.toInventory(itemRegistry),
                actionBarSkills.map { it?.resolve(skillRegistry) as? ActiveSkill }.toTypedArray(),
                zoneReference.resolve(zoneRegistry)
            )
        }
}
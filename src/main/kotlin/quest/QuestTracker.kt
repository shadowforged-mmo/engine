package com.shadowforgedmmo.engine.quest

import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.pack.Namespaces
import net.minestom.server.scoreboard.Sidebar
import net.minestom.server.scoreboard.Sidebar.ScoreboardLine

class QuestTracker(val pc: PlayerCharacter, val progress: QuestProgress) { // TODO: refactor with QuestProgress
    private val completedReferences = progress.completed.toMutableSet()
    private val tracked = progress.tracked.toMutableSet()
    private val objectivesByQuestId = progress.objectivesByQuestId.toMutableMap()

    fun start() {
        updateSidebar()
    }

    fun startQuest(quest: Quest) {
        if (quest.id in objectivesByQuestId) error("Quest already started")

        tracked += quest
        objectivesByQuestId[quest.id] = IntArray(quest.objectives.size)

        pc.entity.showTitle(
            Title.title(
                Component.text("Quest Started", NamedTextColor.YELLOW),
                Component.text(quest.name, NamedTextColor.YELLOW)
            )
        )

        // TODO: clean up
        pc.playSound(
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
        tracked -= quest
        objectivesByQuestId -= quest.id

        pc.entity.showTitle(
            Title.title(
                Component.text("Quest Completed", NamedTextColor.YELLOW),
                Component.text(quest.name, NamedTextColor.YELLOW)
            )
        )

        pc.playSound(
            Sound.sound(
                Key.key(Namespaces.ENGINE_SOUNDS, "quest_complete"),
                Sound.Source.MASTER,
                1.0F,
                1.0F
            )
        )

        updateSidebar()
    }

    fun isInProgress(quest: Quest) = quest.id in objectivesByQuestId

    fun isInProgress(quest: Quest, objectiveIndex: Int) = isInProgress(quest) &&
            getProgress(quest, objectiveIndex) < quest.objectives[objectiveIndex].goal

    fun getProgress(quest: Quest, objectiveIndex: Int) =
        objectivesByQuestId.getValue(quest.id)[objectiveIndex]

    fun addProgress(quest: Quest, objectiveIndex: Int, progress: Int) {
        if (quest.id !in objectivesByQuestId) return
        val oldProgress = objectivesByQuestId.getValue(quest.id)[objectiveIndex]
        val newProgress = (oldProgress + progress)
            .coerceAtMost(quest.objectives[objectiveIndex].goal)
        if (oldProgress == newProgress) return
        objectivesByQuestId.getValue(quest.id)[objectiveIndex] = newProgress
        updateSidebar()
    }

    fun incrementProgress(quest: Quest, objectiveIndex: Int) = addProgress(
        quest,
        objectiveIndex,
        1
    )

    fun isReadyToStart(quest: Quest) = pc.level >= quest.level &&
            quest !in completedReferences &&
            quest.id !in objectivesByQuestId &&
            quest.prerequisiteReferences.all { it in completedReferences }

    fun isReadyToTurnIn(quest: Quest) = quest.id in objectivesByQuestId &&
            quest.objectives.withIndex().all { (index, objective) ->
                getProgress(quest, index) == objective.goal
            }

    private fun updateSidebar() {
        sidebar().addViewer(pc.entity)
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
        tracked.flatMapIndexed(::sidebarQuestContent)

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
}

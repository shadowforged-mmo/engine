package com.shadowforgedmmo.engine.quest

import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.pack.Namespaces
import com.shadowforgedmmo.engine.persistence.QuestData
import net.minestom.server.scoreboard.Sidebar
import net.minestom.server.scoreboard.Sidebar.ScoreboardLine

class QuestTracker(val pc: PlayerCharacter, val data: QuestData) {
    private val completed = data.completed.toMutableSet()
    private val tracked = data.tracked.toMutableSet()
    private val objectives = data.objectives.toMutableMap()

    fun start() {
        updateSidebar()
    }

    fun startQuest(quest: Quest) {
        if (quest.id in objectives) error("Quest already started")

        tracked += quest
        objectives[quest.id] = IntArray(quest.objectives.size)

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
        objectives -= quest.id

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

    fun isInProgress(quest: Quest) = quest.id in objectives

    fun isInProgress(quest: Quest, objectiveIndex: Int) = isInProgress(quest) &&
            getProgress(quest, objectiveIndex) < quest.objectives[objectiveIndex].goal

    fun getProgress(quest: Quest, objectiveIndex: Int) =
        objectives.getValue(quest.id)[objectiveIndex]

    fun addProgress(quest: Quest, objectiveIndex: Int, progress: Int) {
        if (quest.id !in objectives) return
        val oldProgress = objectives.getValue(quest.id)[objectiveIndex]
        val newProgress = (oldProgress + progress)
            .coerceAtMost(quest.objectives[objectiveIndex].goal)
        if (oldProgress == newProgress) return
        objectives.getValue(quest.id)[objectiveIndex] = newProgress
        updateSidebar()
    }

    fun incrementProgress(quest: Quest, objectiveIndex: Int) = addProgress(
        quest,
        objectiveIndex,
        1
    )

    fun isReadyToStart(quest: Quest) = pc.level >= quest.level &&
            quest !in completed &&
            quest.id !in objectives &&
            quest.prerequisiteIds.map(pc.runtime.questsById::getValue).all { it in completed }

    fun isReadyToTurnIn(quest: Quest) = quest.id in objectives &&
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

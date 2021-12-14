package com.matrix.kotodo.todotxt

import android.view.View.GONE
import android.view.View.VISIBLE
import java.time.LocalDate
import java.util.*
import kotlin.collections.HashMap

const val START_NEW_TASKS_TODAY = false


@Suppress("MemberVisibilityCanBePrivate")
class TodoItem(private val itemString: String) {

    companion object {
        private const val descProjectWordReStr = "^\\+[^\\s]+$"
        private const val descContextWordReStr = "^@[^\\s]+$"
        private const val descTaggedWordReStr = "^[^\\s:]+:[^\\s:]+$"
        private const val dateReStr = "^[0-3][0-9]{3}-(0[0-9]|1[0-2])-([0-2][0-9]|3[0-1])$"
        private const val descPriorityStr = "^\\([A-Z]\\)|x$"
        val customTagRegex: Regex = Regex(descTaggedWordReStr)
        val dateRegex: Regex = Regex(dateReStr)
        val projectRegex: Regex = Regex(descProjectWordReStr)
        val contextRegex: Regex = Regex(descContextWordReStr)
        val startRegex: Regex = Regex(descPriorityStr)
    }

    class TaskState(inState: Char? = null) : Comparable<TaskState> {
        var state: Char? = inState

        override fun toString(): String {
            if (this.isFinished()) {
                return "x"
            } else if (this.state == null) {
                return ""
            }
            return "(${this.state})"
        }

        fun markFinished() {
            this.state = 'x'
        }

        fun isFinished(): Boolean {
            return this.state == 'x'
        }

        fun updatePriority(newPriority: Char?) {
            if (newPriority == ' ') {
                this.state = null
            } else {
                this.state = newPriority
            }
        }

        override fun compareTo(other: TaskState): Int {
            val thisState = if (this.state == 'x') {
                Int.MAX_VALUE
            } else {
                this.state?.code ?: Int.MAX_VALUE - 1
            }
            val otherState = if (other.state == 'x') {
                Int.MAX_VALUE
            } else {
                other.state?.code ?: Int.MAX_VALUE - 1
            }
            return compareValues(thisState, otherState)
        }

        override fun equals(other: Any?): Boolean {
            return this.state == (other as TaskState).state
        }

        override fun hashCode(): Int {
            return state?.hashCode() ?: 0
        }
    }

    var description: String = ""
    var startDate: LocalDate? = null
    var finishDate: LocalDate? = null
    var taskState: TaskState = TaskState()
    var objective: MutableList<String> = mutableListOf()
    var contexts: MutableList<String> = mutableListOf()
    var projects: MutableList<String> = mutableListOf()
    var customTags: SortedMap<String, String> = sortedMapOf()
    val initTokens: MutableList<String> =
        itemString.split(Regex("\\s")) as MutableList<String>
    val initString = itemString

    init {
        var index = 0
        while (index < initTokens.size) {
            val currentWord = initTokens[index]
            if (index == 0 && startRegex.matches(currentWord)) {
                if (currentWord.length == 1) {
                    this.taskState.state = currentWord[0]
                } else {
                    this.taskState.state = currentWord[1]
                }
            } else if (dateRegex.matches(currentWord)) {
                if (index <= 1
                    && this.taskState.isFinished()
                    && this.finishDate == null
                ) {
                    this.finishDate = LocalDate.parse(currentWord)
                } else if (index <= 2 && this.startDate == null) {
                    this.startDate = LocalDate.parse(currentWord)
                }
            } else {
                when {
                    projectRegex.matches(currentWord) -> {
                        this.projects.add(currentWord)
                    }
                    contextRegex.matches(currentWord) -> {
                        this.contexts.add(currentWord)
                    }
                    customTagRegex.matches(currentWord) -> {
                        val newPair = currentWord.split(":")
                        this.customTags[newPair[0]] = newPair[1]
                    }
                    else -> {
                        this.objective.add(currentWord)
                    }
                }
            }
            index++
        }
        if (START_NEW_TASKS_TODAY && this.startDate == null) {
            this.startDate = LocalDate.now()
        }
        if (this.taskState.isFinished() && this.finishDate == null) {
            this.finishDate = LocalDate.now()
        }
        this.setDescription()
    }

    fun setPriority(newState: Char?) {
        taskState.updatePriority(newState)
    }

    fun objectiveAsString(): String {
        return if (this.objective.size == 0) {
            ""
        } else {
            this.objective.joinToString(separator = " ")
        }
    }

    fun updateObjective(newObjective: MutableList<String>) {
        this.objective = newObjective
        this.setDescription()
    }

    fun updateStartDate(newStartDate: LocalDate?) {
        this.startDate = newStartDate
    }

    fun updateFinishDate(newFinishDate: LocalDate?) {
        this.finishDate = newFinishDate
    }

    fun addProject(newProject: String) {
        this.projects.add(newProject)
    }

    fun removeProject(oldProject: String) {
        this.projects.remove(oldProject)
    }

    fun addContext(newContext: String) {
        this.contexts.add(newContext)
    }

    fun removeContext(oldContext: String) {
        this.contexts.remove(oldContext)
    }

    fun addCustomTag(newKey: String, newValue: String) {
        this.customTags[newKey] = newValue
    }

    fun contextMap(alLContexts: Set<String>) : SortedMap<String, Boolean> {
        val tempHash = HashMap<String, Boolean>()
        for (context in alLContexts) {
            tempHash[context] = context in this.contexts
        }
        return tempHash.toSortedMap()
    }

    fun projectMap(alLProjects: Set<String>) : SortedMap<String, Boolean> {
        val tempHash = HashMap<String, Boolean>()
        for (project in alLProjects) {
            tempHash[project] = project in this.projects
        }
        return tempHash.toSortedMap()
    }

    private fun setDescription() {
        val tokens = mutableListOf<String>()
        for (item in this.objective) {
            tokens.add(item)
        }
        for (item in this.contexts) {
            tokens.add(item)
        }
        for (item in this.projects) {
            tokens.add(item)
        }
        this.description = tokens.joinToString(separator = " ")
    }

    fun contextsAsString(): String {
        val tokens = mutableListOf<String>()
        for (item in this.contexts) {
            tokens.add(item)
        }
        return tokens.joinToString(separator = " ")
    }

    fun projectsAsString(): String {
        val tokens = mutableListOf<String>()
        for (item in this.projects) {
            tokens.add(item)
        }
        return tokens.joinToString(separator = " ")
    }

    fun startDateAsString(): String {
        return if (this.startDate != null) {
            this.startDate.toString()
        } else {
            ""
        }
    }

    fun finishDateAsString(): String {
        return if (this.finishDate!= null) {
            this.finishDate.toString()
        } else {
            ""
        }
    }

    fun itemStateAsString(): String {
        return this.taskState.toString()
    }

    fun customTagsAsString(): String {
        val tokens = mutableListOf<String>()
        for (pair in this.customTags) {
            tokens.add("${pair.key}:${pair.value}")
        }
        return tokens.joinToString(separator = " ")
    }

    fun hasDates(): Boolean {
        return this.hasStartDate() || this.hasFinishDate()
    }

    fun hasStartDate(): Boolean {
        return this.startDate != null
    }

    fun hasFinishDate(): Boolean {
        return this.finishDate != null
    }

    fun finish(endToday: Boolean = true) {
        if (endToday) {
            this.finishDate = LocalDate.now()
        }
        this.taskState.markFinished()
    }

    fun dateVisibility(): Int {
        return when (this.hasDates()) {
            true -> VISIBLE
            false -> GONE
        }
    }

    fun finishDateVisibility(): Int {
        return when (this.finishDate != null) {
            true -> VISIBLE
            false -> GONE
        }
    }

    fun startDateVisibility(): Int {
        return when (this.startDate != null) {
            true -> VISIBLE
            false -> GONE
        }
    }

    fun contextsVisibility(): Int {
        return when (this.contexts.size != 0) {
            true -> VISIBLE
            false -> GONE
        }
    }

    fun projectsVisibility(): Int {
        return when (this.projects.size != 0) {
            true -> VISIBLE
            false -> GONE
        }
    }

    fun customTagsVisibility(): Int {
        return when (this.customTags.size != 0) {
            true -> VISIBLE
            false -> GONE
        }
    }

    override fun toString(): String {
        val outTokens = mutableListOf<String>()
        if (this.taskState.state != null) {
            outTokens.add(this.taskState.toString())
        }
        if (this.startDate != null) {
            outTokens.add(startDate.toString())
            if (this.finishDate != null) {
                outTokens.add(1, finishDate.toString())
            }
        }
        for (item in this.objective) {
            outTokens.add(item)
        }
        for (item in this.contexts) {
            outTokens.add(item)
        }
        for (item in this.projects) {
            outTokens.add(item)
        }
        return outTokens.joinToString(separator = " ") { it }
    }

    override fun equals(other: Any?): Boolean {
        val otherSafe = other as TodoItem
        return this.itemString == otherSafe.itemString
    }

    override fun hashCode(): Int {
        return itemString.hashCode()
    }
}
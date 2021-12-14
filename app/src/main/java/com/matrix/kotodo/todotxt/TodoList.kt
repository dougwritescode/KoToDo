package com.matrix.kotodo.todotxt

const val DEFAULT_NEW_TAG_DATA_TYPE = "string"
//const val TODO_LIST_LOGGING_TAG = "TodoList"

@Suppress("MemberVisibilityCanBePrivate")
class TodoList {

    companion object {
        val allPriorities = ('A'..'Z').toList()
    }

    var listItems: MutableList<TodoItem> = mutableListOf()
    var availablePriorities = mutableListOf<Char>()
    var allContexts: MutableSet<String> = mutableSetOf()
    var allProjects: MutableSet<String> = mutableSetOf()
    var customTags: HashMap<String, String> = HashMap()

    fun sortAllItems() {
        this.listItems.sortWith(compareBy(
            { it.taskState },
            { it.finishDate },
            { it.startDate }
        ))
    }

    fun addItemFromString(newItemStr: String) {
//        Log.d("Debug", "new Item added to list: '$newItemStr'")
        val newItem = TodoItem(newItemStr)
        this.add(newItem)
        for (context in newItem.contexts) {
            if (context in this.allContexts) {
                continue
            } else {
                this.allContexts.add(context)
            }
        }
        for (project in newItem.projects) {
            if (project in this.allProjects) {
                continue
            } else {
                this.allProjects.add(project)
            }
        }
        for (tag in newItem.customTags) {
            if (tag.key !in this.customTags) {
                this.customTags[tag.key] = DEFAULT_NEW_TAG_DATA_TYPE
            }
        }
        updateAvailablePriorities()
    }

    fun updateAvailablePriorities() {
        val itemsStates = listItems.map{ y -> y.taskState.state }.toTypedArray()
        val prioritySet = mutableSetOf(*itemsStates)
        prioritySet.remove('x')
        val lowestPriority: Char? = if (prioritySet.size > 1) {
            prioritySet.maxOfWith(compareBy{ it }, { it })
        } else {
            'A'
        }
        val prioritiesSize = allPriorities.indexOf(lowestPriority)
        val offsetIndex = if (prioritiesSize < 27) {
            prioritiesSize + 1
        } else {
            prioritiesSize
        }
        val tempList = allPriorities.slice(0..offsetIndex).toMutableList()
        tempList.add('x')
        tempList.add(' ')
        availablePriorities = tempList
    }

    fun addContext(
        newContext: String,
        itemIndex: Int
    ) {
        this.allContexts.add(newContext)
        this.listItems[itemIndex].addContext(newContext)
    }

    fun addProject(
        newProject: String,
        itemIndex: Int
    ) {
        this.allProjects.add(newProject)
        this.listItems[itemIndex].addProject(newProject)
    }

    override fun equals(other: Any?): Boolean {
        val otherList = other as TodoList
        return this.listItems == otherList.listItems
    }

    override fun hashCode(): Int {
        return listItems.hashCode()
    }

    override fun toString(): String {
        //Log.d(TODO_LIST_LOGGING_TAG, "TodoList.toString(): ${this.listItems}")
        return this.listItems.toString()
    }

    fun contains(element: TodoItem): Boolean {
        return element in this.listItems
    }

    operator fun get(index: Int): TodoItem {
        return this.listItems[index]
    }

    fun add(element: TodoItem): Boolean {
        val tempBool = this.listItems.add(element)
        this.sortAllItems()
        return tempBool
    }

    fun append(element: TodoItem): Boolean {
        return listItems.add(element)
    }

    fun clear() {
        this.listItems.clear()
        this.allContexts.clear()
        this.allProjects.clear()
    }

    fun iterator(): MutableIterator<TodoItem> {
        return this.listItems.iterator()
    }

    operator fun set(index: Int, element: TodoItem): TodoItem {
        val tempBool = this.listItems.set(index, element)
        updateAvailablePriorities()
        this.sortAllItems()
        return tempBool
    }
}
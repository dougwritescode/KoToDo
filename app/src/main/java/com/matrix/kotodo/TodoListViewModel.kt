package com.matrix.kotodo

import android.net.Uri
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.matrix.kotodo.todotxt.TodoItem
import com.matrix.kotodo.todotxt.TodoList
import java.time.LocalDate
import java.util.*

/**
 * ViewModel for the application
 *
 * @author dougwritescode@gmail.com
 */
class TodoListViewModel: ViewModel() {

    /**
     * Main TodoList Items
     *
     * @property _currentList main TodoList object
     */
    private var _currentList = MutableLiveData<TodoList>()
    val currentList: LiveData<TodoList> = _currentList

    /**
     * Settings variables
     *
     * @property _startNewTasksToday whether to mark new tasks with today's date
     * @property _endFinishedTasksToday whether to mark newly finished tasks with today's date
     * @property _currentTodoTxtUri uri of the currently selected Todo_txt file
     * @property _currentTodoTxtUriString _currentTodoTxtUri as a string
     */
    private var _startNewTasksToday: MutableLiveData<Boolean> = MutableLiveData()
    private var _endFinishedTasksToday: MutableLiveData<Boolean> = MutableLiveData()
    private var _currentTodoTxtUri: MutableLiveData<Uri> = MutableLiveData()
    private var _currentTodoTxtUriString: MutableLiveData<String> = MutableLiveData()
    val currentTodoTxtUriString: MutableLiveData<String> = _currentTodoTxtUriString

    /**
     * TodoItem editing variables and backings
     *
     * @property _editingObjective objective string livedata (two-way data binding)
     * @property _editingState task completion/priority state
     * @property _editingStartDate task start date
     * @property _editingFinishDate task finish date
     * @property _editingContextMap map of all known contexts to this item's contexts
     * @property _editingProjectMap map of all known contexts to this item's projects
     * @property _editingCustomTagsMap map of all of this item's custom tags
     * @property existingItemIndex tracks whether item being edited already exists in the list (int)
     *      or is a new item (null)
     */
    private var _editingObjective: MutableLiveData<String?> = MutableLiveData()
    private var _editingState: MutableLiveData<TodoItem.TaskState?> = MutableLiveData()
    val editingState: LiveData<TodoItem.TaskState?> = _editingState
    private var _editingStartDate: MutableLiveData<LocalDate?> = MutableLiveData()
    val editingStartDate: LiveData<LocalDate?> = _editingStartDate
    private var _editingFinishDate: MutableLiveData<LocalDate?> = MutableLiveData()
    val editingFinishDate: LiveData<LocalDate?> = _editingFinishDate
    private var _editingContextMap: MutableLiveData<SortedMap<String, Boolean>?> = MutableLiveData()
    val editingContextMap: LiveData<SortedMap<String, Boolean>?> = _editingContextMap
    private var _editingProjectMap: MutableLiveData<SortedMap<String, Boolean>?> = MutableLiveData()
    val editingProjectMap: LiveData<SortedMap<String, Boolean>?> = _editingProjectMap
    private var _editingCustomTagsMap: MutableLiveData<SortedMap<String, String>?> = MutableLiveData()
    val editingCustomTagsMap: LiveData<SortedMap<String, String>?> = _editingCustomTagsMap
    var existingItemIndex: Int? = null
    var straightToEditor: Boolean = false

    /**
     * On initialization, make sure current list is not null
     */
    init {
        resetList()
        resetEditingData()
    }

    /**
     * Getter for two-way data binding on objective edit field
     *
     * @return new String to display in objective field
     */
    fun getEditingObjective(): String? {
        return _editingObjective.value
    }

    /**
     * Setter for two-way data binding on objective edit field
     *
     * @param newString new String to set objective to
     */
    fun setEditingObjective(newString: String?) {
        _editingObjective.value = newString
    }

    /**
     * Clear the main TodoList of all tasks
     */
    fun resetList() {
        _currentList.value = TodoList()
    }

    /**
     * Clear the viewModel editing fields for later use
     */
    private fun resetEditingData() {
        _editingObjective.value = null
        _editingState.value = null
        _editingStartDate.value = null
        _editingFinishDate.value = null
        _editingContextMap.value = null
        _editingProjectMap.value = null
        _editingCustomTagsMap.value = null
        straightToEditor = false
        existingItemIndex = null
    }

    /**
     * Get the edited data as a TodoItem object
     *
     * @return a new TodoItem from the editing data
     */
    private fun editedDataAsItem(): TodoItem {
        val newItem = TodoItem("")
        newItem.objective = _editingObjective.value!!.split(" ").toMutableList()
        newItem.setPriority(_editingState.value!!.state)
        newItem.updateStartDate(_editingStartDate.value)
        newItem.updateFinishDate(_editingFinishDate.value)
        newItem.contexts = _editingContextMap.value!!.filter { it.value }.keys.toMutableList()
        newItem.projects = _editingProjectMap.value!!.filter { it.value }.keys.toMutableList()
        newItem.customTags = _editingCustomTagsMap.value!!
        resetEditingData()
        return newItem
    }

    /**
     * Adds this new TodoItem to the main list as a new item
     */
    fun addEditedDataAsNewListItem() {
        _currentList.value!!.add(editedDataAsItem())
        _currentList.value!!.updateAvailablePriorities()
    }

    /**
     * Overwrites the corresponding existing task with this new data
     */
    fun applyEditedDataToExistingItem() {
        _currentList.value!![existingItemIndex!!] = editedDataAsItem()
        _currentList.value!!.updateAvailablePriorities()
    }

    /**
     * Setter for two-way data binding on [_startNewTasksToday]
     *
     * @param newValue value to update variable to
     */
    fun setStartNewTasksToday(newValue: Boolean) {
        Log.d(
            "SettingsViewModel",
            "setStartNewTasksToday($newValue)"
        )
        _startNewTasksToday.value = newValue
    }

    /**
     * Getter for two-way data binding on [_startNewTasksToday]
     *
     * @return new boolean to update display
     */
    fun getStartNewTasksToday() : Boolean {
        Log.d(
            "SettingsViewModel",
            "getStartNewTasksToday() -> ${_startNewTasksToday.value!!}"
        )
        return _startNewTasksToday.value!!
    }

    /**
     * Setter for two-way data binding on [_endFinishedTasksToday]
     *
     * @param newValue value to update variable to
     */
    fun setEndFinishedTasksToday(newValue: Boolean) {
        Log.d(
            "SettingsViewModel",
            "setEndFinishedTasksToday($newValue)"
        )
        _endFinishedTasksToday.value = newValue
    }

    /**
     * Getter for two-way data binding on [_endFinishedTasksToday]
     *
     * @return new boolean to update display
     */
    fun getEndFinishedTasksToday() : Boolean {
        Log.d(
            "SettingsViewModel",
            "getEndFinishedTasksToday() -> ${_endFinishedTasksToday.value!!}"
        )
        return _endFinishedTasksToday.value!!
    }

    /**
     * Setter for [_currentTodoTxtUri]
     *
     * @param newValue new URI value to set
     */
    fun setCurrentTodoTxtUri(newValue: Uri) {
        Log.d("SettingsViewModel", "setCurrentTodoTxtUri($newValue)")
        _currentTodoTxtUri.value = newValue
        _currentTodoTxtUriString.value = _currentTodoTxtUri.value!!.toString()
    }

    /**
     * Getter for [_currentTodoTxtUri]
     *
     * @return current file selection to load
     */
    fun getCurrentTodoTxtUri() : Uri? {
        Log.d(
            "SettingsViewModel",
            "getEndFinishedTasksToday() -> ${_currentTodoTxtUri.value}"
        )
        return _currentTodoTxtUri.value
    }

    /**
     * Gets whether to display the current file location
     *
     * @return visbility integer value of the current file URI
     */
    fun fileLocationVisibility() : Int {
        Log.d(
            "SettingsViewModel",
            "fileLocationVisibility() -> ${_currentTodoTxtUri is Uri}"
        )
        return when (_currentTodoTxtUri is Uri) {
            true -> View.VISIBLE
            false -> View.GONE
        }
    }

    /**
     * Fills the item editing data with either empty values or an existing item
     *
     * @param item new [TodoItem] to pull data from, otherwise empty fields
     */
    fun initEditItemData(item: TodoItem = TodoItem("")) {
        if (item.objective.joinToString(" ").isNotBlank()) {
            _editingObjective.value = item.objective.joinToString(separator = " ")
            _editingStartDate.value = item.startDate
            _editingFinishDate.value = item.finishDate
        } else {
            _editingObjective.value = ""
            _editingStartDate.value = when (_startNewTasksToday.value!!) {
                true -> LocalDate.now()
                false -> null
            }
            _editingFinishDate.value = null
        }
        _editingState.value = item.taskState
        _editingContextMap.value = item.contextMap(_currentList.value!!.allContexts)
        _editingProjectMap.value = item.projectMap(_currentList.value!!.allProjects)
        _editingCustomTagsMap.value = item.customTags
    }

    /**
     * Gets Whether to display a completion date
     *
     * @return visibility integer value for the finish date display
     */
    fun finishDateVisibility(): Int {
        return when (_editingFinishDate.value) {
            null -> View.GONE
            else -> View.VISIBLE
        }
    }

    /**
     * Setter for the priority / completion state of the edited task
     * also updates finish date if new state is any unfinished state
     *
     * @param newState a [TodoItem.taskState] initializer value
     */
    fun setPriority(newState: Char?) {
        if (_editingState.value == null) {
            _editingState.value = TodoItem.TaskState(newState)
        } else {
            _editingState.value!!.updatePriority(newState)
        }

        if (newState != 'x') {
            _editingFinishDate.value = null
        }
    }

    /**
     * Setter for the start date value of the edited task
     *
     * @param newDate a [LocalDate] value to insert
     */
    fun setStartDate(newDate: LocalDate) {
        _editingStartDate.value = newDate
    }

    /**
     * Setter for the finish date value of the edited task
     *
     * @param newDate a [LocalDate] value to insert
     */
    fun setFinishDate(newDate: LocalDate) {
        _editingFinishDate.value = newDate
    }

    /**
     * Adds a new project tag to project collection
     *
     * @param newProject string to insert into project map as key
     * @param newStatus boolean to insert into project map as value
     */
    fun addProject(newProject: String, newStatus: Boolean = true) {
        _editingProjectMap.value!![newProject] = newStatus
    }

    /**
     * Toggles the boolean value in the Project Map
     *
     * @param projectIndex integer index into project map to toggle
     */
    fun toggleProject(projectIndex: Int) {
        val projectString = _editingProjectMap.value!!.keys.sorted()[projectIndex]
//        Log.d("Debug", "toggleProject($projectIndex, $projectString)")
//        Log.d("Debug", "editingState before: ${_editingProjectMap.value!![projectString]}")
        _editingProjectMap.value!![projectString] = _editingProjectMap.value!![projectString]!!.not()
//        Log.d("Debug", "editingState after: ${_editingProjectMap.value!![projectString]}")
    }

    /**
     * Adds a new project tag to context collection
     *
     * @param newContext string to insert into context map as key
     * @param newStatus boolean to insert into context map as value
     */
    fun addContext(newContext: String, newStatus: Boolean = true) {
        _editingProjectMap.value!![newContext] = newStatus
    }

    /**
     * Toggles the boolean value in the Context Map
     *
     * @param contextIndex integer index into context map to toggle
     */
    fun toggleContext(contextIndex: Int) {
        val contextString = _editingContextMap.value!!.keys.sorted()[contextIndex]
//        Log.d("Debug", "toggleContext($contextIndex, $contextString)")
//        Log.d("Debug", "editingState before: ${_editingContextMap.value!![contextString]}")
        _editingContextMap.value!![contextString] = _editingContextMap.value!![contextString]!!.not()
//        Log.d("Debug", "editingState after: ${_editingContextMap.value!![contextString]}")
    }

    /**
     * Adds a custom tag to current edited task
     *
     * @param newKey string to insert into custom tags map as key
     * @param newValue string to insert into custom tags map as value
     */
    fun addCustomTag(newKey: String, newValue: String) {
        _editingCustomTagsMap.value!![newKey] = newValue
    }
}
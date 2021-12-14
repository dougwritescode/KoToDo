package com.matrix.kotodo.itemedit

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.matrix.kotodo.R
import com.matrix.kotodo.TodoListViewModel
import com.matrix.kotodo.databinding.TodoItemEditFragmentBinding
import java.time.LocalDate
import java.util.*

/**
 * Item Edit display fragment object
 *
 * Contains display logic for editing a TodoItem
 * @author dougwritescode@gmail.com
 */
class TodoItemEditFragment : Fragment() {

    /**
     * @property viewModel LiveData viewmodel for all application data
     * @property binding data binding to the fragment layout
     */
    private val viewModel: TodoListViewModel by activityViewModels()
    private var binding: TodoItemEditFragmentBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataBinding: TodoItemEditFragmentBinding = DataBindingUtil.setContentView(
            this.context as Activity,
            R.layout.todo_item_edit_fragment
        )
        binding = dataBinding
        dataBinding.lifecycleOwner = this
        dataBinding.viewModel = viewModel
        dataBinding.fragment = this

        /**
         * Set up task editingState/priority spinner
         */
        val prioritySpinner = dataBinding.prioritySpinner

        prioritySpinner.adapter = ArrayAdapter(
            context as Activity,
            android.R.layout.simple_spinner_dropdown_item,
            viewModel.currentList.value!!.availablePriorities
        )

        val tempTaskState = viewModel.editingState.value?.state?: ' '
        val posInt = viewModel.currentList.value!!.availablePriorities.indexOf(tempTaskState)
        prioritySpinner.setSelection(posInt)

        prioritySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.setPriority(viewModel.currentList.value!!.availablePriorities[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        /**
         * Set up project tags recyclerview
         */
        dataBinding.projectTagField.adapter = TagFieldAdapter(
            { projectIndex: Int ->
                run {
                    viewModel.toggleProject(projectIndex)
                }
            },
            ContextCompat.getColor(this.context as Activity, R.color.tag_projects)
        )
        dataBinding.projectTagField.layoutManager = TagFlowBoxLayoutManager(context)

        /**
         * Set up context tags recyclerview
         */
        dataBinding.contextTagField.adapter = TagFieldAdapter(
            { contextIndex: Int ->
                run {
                    viewModel.toggleContext(contextIndex)
                }
            },
            ContextCompat.getColor(this.context as Activity, R.color.tag_contexts)
        )
        dataBinding.contextTagField.layoutManager = TagFlowBoxLayoutManager(context)

        /**
         * Set up custom tag recyclerview
         */
        dataBinding.customTagField.adapter = CustomTagFieldAdapter(
            {},
            ContextCompat.getColor(this.context as Activity, R.color.tag_customs)
        )
        dataBinding.customTagField.layoutManager = TagFlowBoxLayoutManager(context)

        val addItemFAB = binding!!
            .root
            .findViewById<FloatingActionButton>(R.id.confirm_floating_action_button)
        addItemFAB.setOnClickListener { confirmChangesAndExit() }
    }

//    override fun onPause() {
//        super.onPause()
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val rootView = inflater.inflate(
            R.layout.todo_item_edit_fragment,
            container,
            false
        )
        val sharedText = arguments?.getString("SHARED_OBJECTIVE")
        rootView.findViewById<EditText>(R.id.objective_edit_field).setText(sharedText)
        return rootView
    }

    /**
     * Stores changes to main TodoList
     */
    private fun confirmChanges() {
//        Log.d("Debug", "confirmChanges() invoked")
        if (binding!!.objectiveEditField.text.toString().isNotBlank()) {
            Log.d("Debug","text field is not blank!")
            if (viewModel.existingItemIndex != null) {
                viewModel.applyEditedDataToExistingItem()
            } else {
                viewModel.addEditedDataAsNewListItem()
            }
        }
    }

    /**
     * Stores changes to main TodoList then return to main list view
     */
    private fun confirmChangesAndExit() {
//        Log.d("Debug", "confirmChangesAndExit() invoked")
        confirmChanges()
        findNavController().navigate(R.id.action_todoItemEditFragment_to_mainListFragment)
    }

    /**
     * Pulls up a date selector Dialog to choose a date and apply chosen date to one field
     *
     * @param editingStartDate Boolean stating whether the date to edit is the start date or
     *      finish date.
     */
    fun editDate(editingStartDate: Boolean = true) {

//        Log.d("Debug","editDate($editingStartDate)")
        val startYear: Int
        val startMonth: Int
        val startDay: Int
        val currentDate: Calendar = Calendar.getInstance()

        if (viewModel.editingStartDate.value != null && editingStartDate) {
            startYear = viewModel.editingStartDate.value!!.year
            startMonth = viewModel.editingStartDate.value!!.monthValue - 1
            startDay = viewModel.editingStartDate.value!!.dayOfMonth
        } else if (viewModel.editingFinishDate.value != null && !editingStartDate) {
            startYear = viewModel.editingFinishDate.value!!.year
            startMonth = viewModel.editingFinishDate.value!!.monthValue - 1
            startDay = viewModel.editingFinishDate.value!!.dayOfMonth
        } else {
            startYear = currentDate.get(Calendar.YEAR)
            startMonth = currentDate.get(Calendar.MONTH)
            startDay = currentDate.get(Calendar.DAY_OF_MONTH)
        }

        val datePicker = DatePickerDialog(
            this.context as Activity,
            { _, year, month, day ->
                val newDate =
                    LocalDate.of(
                        year,
                        month + 1,
                        day
                    )
                when (editingStartDate) {
                    true -> viewModel.setStartDate(newDate)
                    false -> viewModel.setFinishDate(newDate)
                }
            },
            startYear,
            startMonth,
            startDay
        )
        datePicker.show()
    }

    /**
     * Add a new Custom tag and apply it to this task
     */
    fun addCustomTag() {
        val tagAdd = CustomTagAddDialogFragment(
            "",
            "",
        ) {
                newKey: String, newValue: String ->
            val underscoredKey = newKey.replace(' ', '_')
            if (underscoredKey !in viewModel.currentList.value!!.customTags.keys) {
                val underscoredValue = newValue.replace(' ', '_')
                viewModel.currentList.value!!.customTags[underscoredKey] = underscoredValue
//            Log.d("Debug","custom tags before insert: ${viewModel.editingCustomTagsMap.value}")
                viewModel.addCustomTag(underscoredKey, underscoredValue)
//            Log.d("Debug","custom tags after insert: ${viewModel.editingCustomTagsMap.value}")
                (binding!!.customTagField.adapter as CustomTagFieldAdapter)
                    .addItem(underscoredKey, underscoredValue)
            }
        }
        tagAdd.show(childFragmentManager, "ADD_CUSTOM_TAG")
    }

    /**
     * Add a new project tag and apply it to this task
     */
    fun addProject() {
        val tagAdd = SingleTagAddDialogFragment(
            "+",
            ""
        ) { newString: String ->
            val underscoredString = newString.replace(' ', '_')
            if (underscoredString !in viewModel.currentList.value!!.allProjects) {
                viewModel.currentList.value!!.allProjects.add(underscoredString)
//                Log.d("Debug","projects before insert: ${viewModel.editingProjectMap.value}")
                viewModel.addProject(underscoredString)
//                Log.d("Debug","projects after insert: ${viewModel.editingProjectMap.value}")
                (binding!!.projectTagField.adapter as TagFieldAdapter)
                    .addItem(Pair(underscoredString, true))
            }
        }
        tagAdd.show(childFragmentManager, "ADD_PROJECT")
    }

    /**
     * Add a new custom tag and apply it to this task
     */
    fun addContext() {
        val tagAdd = SingleTagAddDialogFragment(
            "@",
            ""
        ) { newString: String ->
            val underscoredString = newString.replace(' ', '_')
            if (underscoredString !in viewModel.currentList.value!!.allContexts) {
                viewModel.currentList.value!!.allContexts.add(underscoredString)
//                Log.d("Debug","contexts before insert: ${viewModel.editingContextMap.value}")
                viewModel.addContext(underscoredString)
//                Log.d("Debug","contexts after insert: ${viewModel.editingContextMap.value}")
                (binding!!.contextTagField.adapter as TagFieldAdapter)
                    .addItem(Pair(underscoredString, true))
            }
        }
        tagAdd.show(childFragmentManager, "ADD_CONTEXT")
    }
}
package com.matrix.kotodo.mainlist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.matrix.kotodo.R
import com.matrix.kotodo.TodoListViewModel
import com.matrix.kotodo.databinding.ListFragmentBinding
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * Main TodoList display fragment object
 *
 * Contains display and switching logic for main TodoList
 * @author dougwritescode@gmail.com
 */
class ListFragment : Fragment() {

    /**
     * @property binding data binding to the fragment layout
     * @property viewModel LiveData viewmodel for all application data
     * @property listItemAdapter adapter for the recyclerview in the main list
     */
    private var binding: ListFragmentBinding? = null
    private val viewModel: TodoListViewModel by activityViewModels()
    private val listItemAdapter = TodoListItemAdapter(
        { viewIndex: Int -> editExistingItem(viewIndex) },
        { viewIndex: Int ->
            run {
                viewModel.currentList.value!![viewIndex].finish(true)
                viewModel.currentList.value!!.sortAllItems()
                (this.binding!!.todoListRecyclerView.adapter!! as TodoListItemAdapter)
                    .submitList(viewModel.currentList.value!!.listItems)
//                this.binding!!.todoListRecyclerView.adapter!!.notifyDataSetChanged()
                val itemLen = this.binding!!.todoListRecyclerView.adapter!!.itemCount
                this.binding!!.todoListRecyclerView.adapter!!.notifyItemRangeChanged(0, itemLen)
                viewModel.currentList.value!!.updateAvailablePriorities()
            }
        }
    )

//    private inner class DeferredWriteWorker(
//        context: Context,
//        params: WorkerParameters
//    ): Worker(
//        context,
//        params
//    ) {
//
//        init {
//            startWork()
//        }
//
//        override fun doWork(): Result {
//            val uriString = viewModel.currentTodoTxtUriString.value!!
//            Log.d("Debug", "writeListToFile($uriString)")
//            val uri = Uri.parse(uriString)
//            val contentResolver = (requireContext() as Activity).contentResolver
//            try {
//                contentResolver.takePersistableUriPermission(
//                    uri,
//                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
//                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//                )
//            } catch (e: SecurityException) {
//                Log.d("Debug", "No persistable permission found")
//                return Result.failure()
//            }
//            for (item in viewModel.currentList.value!!.listItems) {
//                Log.d("Debug", "list items before writing: ${item.description}")
//            }
//
//            /**
//             * Write current list to file output.
//             */
//            contentResolver.openOutputStream(Uri.parse(uriString), "rwt")?.use { outputStream ->
//                BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
//                    for (item in viewModel.currentList.value!!.listItems) {
//                        if (item.toString() !in listOf("", " ", "\n")) {
//                            Log.d("Debug", "writing line to file: $item")
//                            writer.append(item.toString())
//                            writer.appendLine()
//                        }
//                    }
//                    writer.close()
//                }
//                outputStream.close()
//            }
//            return Result.success()
//
//            writeListToFile()
//            return Result.success()
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * Get dataBinding to layout and bind it, apply itemAdapter
         */
        val dataBinding: ListFragmentBinding = DataBindingUtil.setContentView(
            this.context as Activity,
            R.layout.list_fragment
        )
        binding = dataBinding
        dataBinding.todoListRecyclerView.adapter = listItemAdapter

        /**
         * Load reference to sharedPreferences to set local settings livedata
         */
        val sharedPreferences = activity?.getSharedPreferences(
            getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        ) ?: return

        /**
         * Set local viewModel variables from sharedPreferences Object
         */
        viewModel.setStartNewTasksToday(
            sharedPreferences.getBoolean(
                getString(R.string.start_new_tasks_today_settings_key),
                true
            )
        )
        viewModel.setEndFinishedTasksToday(
            sharedPreferences.getBoolean(
                getString(R.string.end_finished_tasks_today_settings_key),
                true
            )
        )

        /**
         * Load current list if we have a reference to one
         */
        val uriString = sharedPreferences.getString(
            getString(R.string.current_todo_txt_URI_settings_key),
            null
        )
        if (
            uriString != null &&
            viewModel.currentList.value!!.listItems.size == 0
        ) {
            viewModel.setCurrentTodoTxtUri(Uri.parse(uriString))
//            Log.d("Debug","Loading list from file at ListFragment.onCreate()")
            readListFromFile(uriString)
        }

//        /**
//         * Get reference to of main activity for handling adding tasks
//         */
//        val intent = Intent(this.context, MainActivity::class.java)
//        Log.d("Debug", "intent: $intent")
//        Log.d("Debug", "intent action: ${intent.action}")
//        Log.d("Debug", "found intent extra string: ${intent.getStringExtra(Intent.EXTRA_TEXT)}")
//        /**
//         * If we have a sent text piece, add it as a new task
//         */
//        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
//            addNewItem(intent.getStringExtra(Intent.EXTRA_TEXT))
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val fragmentBinding = ListFragmentBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI
            .onNavDestinationSelected(
                item,
                requireView().findNavController()
            ) || super.onOptionsItemSelected(item)
    }

//    override fun onPause() {
//        super.onPause()
//        Log.d("Debug","ListFragment.onPause()")
//        writeListToFile(viewModel.getCurrentTodoTxtUriString())
//    }

    override fun onStop() {
        super.onStop()
//        Log.d("Debug", "ListFragment.onStop()")
        if (viewModel.currentTodoTxtUriString.value != null) {
            writeListToFile(viewModel.currentTodoTxtUriString.value!!)
        }
//        Log.d("Debug", "Starting WorkManager")
//        val constraints = Constraints.Builder()
//            .setRequiresStorageNotLow(true)
//            .build()
//        val writeRequest: WorkRequest = OneTimeWorkRequestBuilder<DeferredWriteWorker>()
//            .setConstraints(constraints)
//            .build()
//        WorkManager
//            .getInstance(requireContext())
//            .enqueue(writeRequest)
//        Log.d("Debug", "Work Request Submitted")
    }

    override fun onResume() {
//        Log.d("Debug", "ListFragment.onResume()")
        super.onResume()
        val dataBinding: ListFragmentBinding = DataBindingUtil.setContentView(
            this.context as Activity,
            R.layout.list_fragment
        )
        binding = dataBinding
        dataBinding.todoListRecyclerView.adapter = listItemAdapter
        binding!!.viewModel = viewModel
        binding!!.listFragment = this
        val addItemFAB = binding!!
            .root
            .findViewById<FloatingActionButton>(R.id.item_add_floating_action_button)
        addItemFAB.setOnClickListener { addNewItem() }
        if (
            viewModel.getCurrentTodoTxtUri() != null
            && viewModel.currentList.value!!.listItems.size == 0
        ) {
//            Log.d("Debug","Loading list from file at ListFragment.onResume()")
//            readListFromFile(viewModel.getCurrentTodoTxtUriString())
            readListFromFile(viewModel.currentTodoTxtUriString.value!!)
        }
        viewModel.currentList.value!!.sortAllItems()
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.straightToEditor) {
            addNewItem(viewModel.getEditingObjective())
        }
    }

    /**
     * Redirects to the item edit fragment with blank data
     *
     * @param optionalTaskString optional string to fill objective editing data
     */
    private fun addNewItem(optionalTaskString: String? = null) {
        viewModel.initEditItemData()
        viewModel.existingItemIndex = null
        if (optionalTaskString != null) {
            viewModel.setEditingObjective(optionalTaskString)
        }
        val action = ListFragmentDirections
            .actionMainListFragmentToTodoItemEditFragment()
        findNavController().navigate(action)
    }

    /**
     * Redirects to the item edit fragment with an existing TodoItem
     *
     * @param listIndex index of item to edit in current TodoList
     */
    private fun editExistingItem(listIndex: Int) {
        viewModel.initEditItemData(viewModel.currentList.value!![listIndex])
        viewModel.existingItemIndex = listIndex
        val action = ListFragmentDirections
            .actionMainListFragmentToTodoItemEditFragment()
        findNavController().navigate(action)
    }

    /**
     * Loads the specified file into the main TodoList recyclerview
     *
     * @param uriString the uri of the target file as a string
     */
    private fun readListFromFile(uriString: String) {
//        Log.d("Debug", "readListFromFile($uriString)")
        // empty list before reading
        viewModel.resetList()
        val uri = Uri.parse(uriString)
        val contentResolver = (this.context as Activity).contentResolver
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (e: SecurityException) {
//            Log.d("Debug", "No persistable permission found")
            return
        }
        contentResolver.openInputStream(Uri.parse(uriString))?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    if (line !in listOf("", " ", "\n")) {
//                        Log.d("Debug", "reading line from file: $line")
                        viewModel.currentList.value!!.addItemFromString(line)
                        line = reader.readLine()
                    }
                }
                reader.close()
            }
            inputStream.close()
        }
//        for (item in viewModel.currentList.value!!.listItems) {
//            Log.d("Debug", "list items after reading: ${item.description}")
//        }
    }

    /**
     * Writes the main TodoList into the specified file
     *
     * @param uriString the uri of the target file as a string
     */
    private fun writeListToFile(uriString: String) {
//        Log.d("Debug", "writeListToFile($uriString)")
        val uri = Uri.parse(uriString)
        val contentResolver = (this.context as Activity).contentResolver
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (e: SecurityException) {
//            Log.d("Debug", "No persistable permission found")
            return
        }
//        for (item in viewModel.currentList.value!!.listItems) {
//            Log.d("Debug", "list items before writing: ${item.description}")
//        }

        /**
         * Write current list to file output.
         */
        contentResolver.openOutputStream(Uri.parse(uriString), "rwt")?.use { outputStream ->
            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                for (item in viewModel.currentList.value!!.listItems) {
                    if (item.toString() !in listOf("", " ", "\n")) {
//                        Log.d("Debug", "writing line to file: $item")
                        writer.append(item.toString())
                        writer.appendLine()
                    }
                }
                writer.close()
            }
            outputStream.close()
        }
    }

//    private fun goToSettings() {
//        findNavController().navigate(R.id.action_mainListFragment_to_settingsFragment)
//    }
}
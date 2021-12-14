package com.matrix.kotodo.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.matrix.kotodo.R
import com.matrix.kotodo.TodoListViewModel
import com.matrix.kotodo.databinding.SettingsFragmentBinding


class SettingsFragment : Fragment() {

    companion object {
        const val CREATE_FILE_REQUEST_CODE = 1
        const val PICK_FILE_REQUEST_CODE = 2
    }

    private var binding: SettingsFragmentBinding? = null
    private val viewModel: TodoListViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataBinding: SettingsFragmentBinding = DataBindingUtil.setContentView(
            this.context as Activity,
            R.layout.settings_fragment
        )
        binding = dataBinding
        binding!!.fragment = this
        binding!!.viewModel = viewModel
    }

    @SuppressLint("CommitPrefEdits")
    override fun onPause() {
        super.onPause()

        val sharedPreferences = activity?.getSharedPreferences(
            getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        ) ?: return

        with (sharedPreferences.edit()) {
            putBoolean(
                getString(R.string.start_new_tasks_today_settings_key),
                viewModel.getStartNewTasksToday()
            )
            putBoolean(
                getString(R.string.end_finished_tasks_today_settings_key),
                viewModel.getEndFinishedTasksToday()
            )
            putString(
                getString(R.string.current_todo_txt_URI_settings_key),
//                viewModel.getCurrentTodoTxtUriString()
                viewModel.currentTodoTxtUriString.value
            )
            apply()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val fragmentBinding = SettingsFragmentBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        binding!!.viewModel = this.viewModel
//    }

    private fun createFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "todo.txt")
//            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivity(intent)
    }

    private fun importFile() {
//        Log.d("Debug", "importFile() invoked!")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
//            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivity(intent)
    }

    /**
     * Wrapping function around the open or create dialog operation
     */
    fun openOrCreate() {
        val openOrCreate = CreateOrOpenDialogFragment(
            { createFile() },
            { importFile() }
        )
        openOrCreate.show(childFragmentManager, "OPEN_OR_CREATE")
    }

    /**
     * Catch file selection intent result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val contentResolver = this.context?.contentResolver

//        Log.d("Debug", "onActivityResult($requestCode) triggered!")

        if (
            resultCode == Activity.RESULT_OK &&
            requestCode in listOf(PICK_FILE_REQUEST_CODE, CREATE_FILE_REQUEST_CODE)
        ) {
            data?.data?.also {
                uri ->
                    Toast.makeText(
                        this.context,
                        "Todo file set: $uri",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.setCurrentTodoTxtUri(uri)
                    contentResolver?.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
            }
        }
    }
}
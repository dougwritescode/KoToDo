package com.matrix.kotodo.itemedit

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.matrix.kotodo.R

class SingleTagAddDialogFragment(
    private val tagPrefix: String = "",
    private val existingString: String = "",
    private val onConfirm: (String) -> Unit
): DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(
            R.layout.single_tag_add_dialog_fragment,
            container,
            false
        )
        val tagType = root.findViewById<TextView>(R.id.tag_type)
        tagType.text = tagPrefix
        val editField = root.findViewById<EditText>(R.id.tag_add_edit_text)
        editField.setText(existingString)
        val cancelButton = root.findViewById<Button>(R.id.tag_add_cancel_button)
        cancelButton.setOnClickListener {
            dismiss()
        }
        val confirmButton = root.findViewById<Button>(R.id.tag_add_confirm_button)
        confirmButton.setOnClickListener {
            onConfirm(tagPrefix + editField.text.toString())
            dismiss()
        }
        return root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }
}
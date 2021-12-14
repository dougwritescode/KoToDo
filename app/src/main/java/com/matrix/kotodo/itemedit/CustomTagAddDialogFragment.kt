package com.matrix.kotodo.itemedit

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.matrix.kotodo.R

class CustomTagAddDialogFragment(
    private val existingKey: String = "",
    private val existingValue: String = "",
    private val onConfirm: (String, String) -> Unit
): DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(
            R.layout.custom_tag_add_dialog_fragment,
            container,
            false
        )
        val keyEditField = root.findViewById<EditText>(R.id.key_add_edit_text)
        keyEditField.setText(existingKey)
        val valueEditField = root.findViewById<EditText>(R.id.value_add_edit_text)
        valueEditField.setText(existingValue)
        val cancelButton = root.findViewById<Button>(R.id.tag_add_cancel_button)
        cancelButton.setOnClickListener {
            dismiss()
        }
        val confirmButton = root.findViewById<Button>(R.id.tag_add_confirm_button)
        confirmButton.setOnClickListener {
            onConfirm(keyEditField.text.toString(), valueEditField.text.toString())
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
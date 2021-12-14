package com.matrix.kotodo.settings

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import com.matrix.kotodo.R

class CreateOrOpenDialogFragment(
    val createFun: () -> Unit,
    val importFun: () -> Unit
): DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(
            R.layout.create_or_open_dialog_fragment,
            container,
            false
        )
        val createButton = root.findViewById<ImageButton>(R.id.create_button)
        createButton.setOnClickListener{
            createFun()
            dismiss()
        }
        val importButton = root.findViewById<ImageButton>(R.id.import_button)
        importButton.setOnClickListener{
            importFun()
            dismiss()
        }
        return root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }
}
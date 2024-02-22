package com.example.notemake.Displays.EditWordsDisplay

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment

class WordFragment : DialogFragment() {

    var onWordsEntered: ((String, String) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)

        // Set up the inputs
        val input1 = EditText(context)
        input1.inputType = InputType.TYPE_CLASS_TEXT
        val input2 = EditText(context)
        input2.inputType = InputType.TYPE_CLASS_TEXT

        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(input1)
        layout.addView(input2)
        builder.setView(layout)

        // Set up the buttons
        builder.setPositiveButton("OK", null) // null listener
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        val dialog = builder.create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = false // Disable initially

            // Set up a text watcher to enable the positive button when both fields have text
            val textWatcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    positiveButton.isEnabled = input1.text.isNotEmpty() && input2.text.isNotEmpty()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            }

            input1.addTextChangedListener(textWatcher)
            input2.addTextChangedListener(textWatcher)

            positiveButton.setOnClickListener {
                onWordsEntered?.invoke(input1.text.toString(), input2.text.toString())
                dialog.dismiss()
            }
        }

        return dialog
    }
}
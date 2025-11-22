package com.projects.allnotificationblocker.blockthemall.Dialogs

import android.app.*
import android.content.*
import android.graphics.*
import android.graphics.drawable.*
import android.os.*
import android.view.*
import android.widget.*
import com.projects.allnotificationblocker.blockthemall.*
import java.util.*


class ProfileNameDialog(context: Context, private val title: String?): Dialog(context),
    View.OnClickListener {
    var listener: ProfileNameDialogListener? = null
    var name: String = ""
        private set
    var description: String = ""
        private set
    private var textInputLayoutEnterProfileName: EditText? = null
    private var textInputLayoutEnterProfileDescription: EditText? = null

    init {
        if (context is ProfileNameDialogListener) {
            listener = context as ProfileNameDialogListener
        } else {
            throw RuntimeException(
                context
                    .toString() + " must implement ProfileNameDialogListener"
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_new_profile)

        Objects.requireNonNull<Window?>(window)
            .setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        initView()
    }

    private fun initView() {
        val textViewTitle = findViewById<TextView>(R.id.text_view_title)
        textInputLayoutEnterProfileName =
            findViewById<EditText>(R.id.text_input_layout_enter_profile_name)

        textInputLayoutEnterProfileDescription =
            findViewById<EditText>(R.id.text_input_layout_enter_profile_description)
        val mYesButton = findViewById<Button>(R.id.button_yes)
        mYesButton.setOnClickListener(this)

        textViewTitle.text = title
    }


    override fun onClick(v: View) {
        if (v.id == R.id.button_yes) {
            name = textInputLayoutEnterProfileName!!.getText().toString()
            textInputLayoutEnterProfileName!!.requestFocus()
            description = textInputLayoutEnterProfileDescription!!.getText().toString()
            if (name.isEmpty()) {
                Toast.makeText(context, R.string.error_empty_profile_name, Toast.LENGTH_SHORT)
                    .show()
            } else {
                dismiss()
            }
        }
    }

    interface ProfileNameDialogListener {
        fun isProfileNameExist(name: String?): Boolean
    }
}

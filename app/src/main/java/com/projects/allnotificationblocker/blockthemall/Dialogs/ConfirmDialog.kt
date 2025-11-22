package com.projects.allnotificationblocker.blockthemall.Dialogs

import android.app.*
import android.content.*
import android.graphics.*
import android.os.*
import android.view.*
import android.widget.*
import androidx.core.graphics.drawable.*
import com.projects.allnotificationblocker.blockthemall.*


class ConfirmDialog(context: Context, private val title: String?): Dialog(context),
    View.OnClickListener {
    var result: Boolean = false
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_confirm)
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        initView()
    }

    private fun initView() {
        val mViewTitleText = findViewById<TextView>(R.id.text_view_title)
        mViewTitleText.text = title
        val mYesButton = findViewById<Button>(R.id.button_yes)
        mYesButton.setOnClickListener(this)
        val mNoButton = findViewById<Button>(R.id.button_no)
        mNoButton.setOnClickListener(this)
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_yes -> {
                result = true
                dismiss()
            }

            R.id.button_no -> {
                result = false
                dismiss()
            }

            else -> {}
        }
    }
}

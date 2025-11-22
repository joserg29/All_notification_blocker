package com.projects.allnotificationblocker.blockthemall.Dialogs

import android.app.*
import android.content.*
import android.graphics.*
import android.os.*
import android.view.*
import android.widget.*
import androidx.core.graphics.drawable.*
import com.projects.allnotificationblocker.blockthemall.*


class InfoDialog(context: Context, private val title: String?, private val description: String?):
    Dialog(context), View.OnClickListener {
    var result: Boolean = false
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_info)
        // 1.  Safe call operator and `let` for null handling
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        // 2. Call to initView method
        initView()
    }

    private fun initView() {
        val mViewTitleText = findViewById<TextView>(R.id.text_view_title)
        val mViewDescriptionText = findViewById<TextView>(R.id.text_view_description)
        mViewTitleText.text = title
        mViewDescriptionText.text = description
        val mYesButton = findViewById<Button>(R.id.button_yes)
        mYesButton.setOnClickListener(this)
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

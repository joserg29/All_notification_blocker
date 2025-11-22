package com.projects.allnotificationblocker.blockthemall.Activities

import android.content.*
import android.net.*
import android.os.*
import androidx.activity.*
import androidx.appcompat.app.*
import androidx.core.net.*
import androidx.core.view.*
import com.projects.allnotificationblocker.blockthemall.R
import com.projects.allnotificationblocker.blockthemall.Utilities.Util.enableEdgeToEdge16
import com.projects.allnotificationblocker.blockthemall.databinding.*

class AboutActivity: AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge16(binding.root)
        binding.apply {
            tvGmail.setOnClickListener {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:sourcecode777@gmail.com".toUri()
                    putExtra(Intent.EXTRA_SUBJECT, "About App Inquiry")
                }
                startActivity(Intent.createChooser(intent, "Send Email"))
            }
            tvGitHub.setOnClickListener {
                openUrl("https://github.com/")
            }
            tvLinkedIn.setOnClickListener {
                openUrl("https://linkedin.com/")
            }
        }
    }


    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
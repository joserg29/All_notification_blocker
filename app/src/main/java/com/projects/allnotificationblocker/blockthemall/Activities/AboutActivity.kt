package com.projects.allnotificationblocker.blockthemall.Activities

import android.content.*
import android.net.*
import android.os.*
import android.view.*
import android.widget.*
import androidx.activity.*
import androidx.appcompat.app.*
import androidx.core.content.*
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
        
        setupToolbar()
        setupClickListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = getString(R.string.about)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupClickListeners() {
        binding.apply {
            // Gmail contact
            cardGmail.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "mailto:blue.house2920@gmail.com".toUri()
                        putExtra(Intent.EXTRA_SUBJECT, "About App Inquiry")
                        putExtra(Intent.EXTRA_TEXT, "Hello,\n\n")
                    }
                    startActivity(Intent.createChooser(intent, getString(R.string.contact_via_gmail)))
                } catch (e: Exception) {
                    Toast.makeText(this@AboutActivity, "Email app not found", Toast.LENGTH_SHORT).show()
                }
            }
            
            // GitHub profile
            cardGitHub.setOnClickListener {
                openUrl("https://github.com/joserg29")
            }
            
            // LinkedIn profile
            cardLinkedIn.setOnClickListener {
                openUrl("https://www.linkedin.com/in/maksym-iskariev-104b7a206/")
            }
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open link", Toast.LENGTH_SHORT).show()
        }
    }
}
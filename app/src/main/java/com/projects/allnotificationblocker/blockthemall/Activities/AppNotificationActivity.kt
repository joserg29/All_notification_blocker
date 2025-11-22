package com.projects.allnotificationblocker.blockthemall.Activities

import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.activity.*
import androidx.appcompat.app.*
import androidx.core.view.*
import androidx.lifecycle.*
import androidx.recyclerview.widget.*
import com.google.android.material.datepicker.*
import com.projects.allnotificationblocker.blockthemall.Application.MyApplication.Companion.notificationRepo
import com.projects.allnotificationblocker.blockthemall.Fragments.Notifications.*
import com.projects.allnotificationblocker.blockthemall.Fragments.Notifications.NotificationsAdapter.*
import com.projects.allnotificationblocker.blockthemall.R
import com.projects.allnotificationblocker.blockthemall.Utilities.DateTimeUtil.getDateString
import com.projects.allnotificationblocker.blockthemall.Utilities.DateTimeUtil.getDuration
import com.projects.allnotificationblocker.blockthemall.Utilities.Util.enableEdgeToEdge16
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*
import com.projects.allnotificationblocker.blockthemall.databinding.*
import kotlinx.coroutines.*
import java.text.*
import java.util.*
import kotlin.text.isEmpty

class AppNotificationActivity: AppCompatActivity(), NotificationsAdapterListener {
    private lateinit var binding: ActivityAppNotificationBinding
    private lateinit var packageName: String
    private lateinit var adapter: NotificationsAdapter
    private var filteredNot: MutableList<NotificationInfo>? = null
    private var from: Date? = null
    private var to: Date? = null

    companion object {
        const val PACKAGE_EXTRA = "package"
        const val NAME_EXTRA = "name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAppNotificationBinding.inflate(layoutInflater)
        enableEdgeToEdge16(binding.root)
        packageName = intent.getStringExtra(PACKAGE_EXTRA)!!
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.dateRangeBtn.setOnClickListener {
            selectDateTime()
        }

        setAdapter()
        binding.appNameTv.text = intent.getStringExtra(NAME_EXTRA)!!
        binding.editTextSearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                if (editable.isEmpty()) {
                    adapter.userFilter = null
                } else {
                    adapter.userFilter = editable.toString()
                }
                adapter.notifyDataSetChanged()
            }
        })
    }

    private fun setAdapter() {
        val notificationsLive = notificationRepo.getAllRecordsLive()
        adapter = NotificationsAdapter(this, mutableListOf())
        val layoutManager = LinearLayoutManager(this)
        binding.statRv.layoutManager = layoutManager
        binding.statRv.adapter = adapter
        lifecycleScope.launch(Dispatchers.IO) {
            filteredNot = notificationRepo.getAllRecords().filter { it.packageName == packageName }
                .toMutableList()
            adapter.notifications = filteredNot!!
            withContext(Dispatchers.Main) {
                adapter.notifyDataSetChanged()
                if (adapter.notifications.isNotEmpty()) {
                    //todo hideLoading()
                } else {
                    //todo  showNoNotifications()
                }
            }
        }
        notificationsLive.observe(this) {
            if (binding.statRv.adapter == null) {
                return@observe
            }
            lifecycleScope.launch(Dispatchers.Main) {
                adapter.notifications = it.filter { it.packageName == packageName }.toMutableList()
                adapter.notifyDataSetChanged()
                if (it.isNotEmpty()) {
                    //todo  hideLoading()
                } else {
                    //todo  showNoNotifications()
                }
            }
        }

// Timber
    }

    private fun selectDateTime() {
        // Build the picker
        val dateRangePicker = MaterialDatePicker.Builder
            .dateRangePicker().setInputMode(
                MaterialDatePicker.INPUT_MODE_CALENDAR
            )
            .setTitleText("Select Date Range")
            .setTheme(R.style.ThemeOverlay_App_DatePicker)

            .build()
        // Show it
        // Listen for positive click (user tapped “OK”)
        dateRangePicker.addOnPositiveButtonClickListener { selection: androidx.core.util.Pair<Long, Long> ->
            // selection.first = startMillis, selection.second = endMillis
            from = Date(selection.first)
            to = Date(selection.second)
            if (from != null && to != null) {
                adapter.notifications =
                    filteredNot!!.filter { it.postTime!!.toLong() >= from!!.time && it.postTime!!.toLong() <= to!!.time }
                        .toMutableList()
                binding.durationTv.text =
                    getDuration(from!!.getDateString(), to!!.getDateString())
                binding.fromTv.text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(from!!)
                binding.toTv.text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(to!!)
                binding.datesLyt.visibility = View.VISIBLE
                adapter.notifyDataSetChanged()
            }
        }

        dateRangePicker.show(supportFragmentManager, "DATE_RANGE_PICKER")


    }

    override val notificationsList: MutableList<NotificationInfo?>?
        get() = mutableListOf()

    override fun removeNotification(myNotification: NotificationInfo?) {

    }

}
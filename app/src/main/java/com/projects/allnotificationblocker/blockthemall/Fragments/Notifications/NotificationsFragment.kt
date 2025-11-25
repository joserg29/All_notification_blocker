package com.projects.allnotificationblocker.blockthemall.Fragments.Notifications

import android.content.*
import android.os.*
import android.text.*
import android.view.*
import android.widget.*
import androidx.core.content.*
import androidx.fragment.app.*
import androidx.lifecycle.*
import androidx.recyclerview.widget.*
import com.google.android.gms.ads.*
import com.pixplicity.easyprefs.library.*
import com.projects.allnotificationblocker.blockthemall.Application.*
import com.projects.allnotificationblocker.blockthemall.BuildConfig
import com.projects.allnotificationblocker.blockthemall.Dialogs.*
import com.projects.allnotificationblocker.blockthemall.R
import com.projects.allnotificationblocker.blockthemall.Utilities.*
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*
import com.projects.allnotificationblocker.blockthemall.data.repo.*
import kotlinx.coroutines.*
import timber.log.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [NotificationsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NotificationsFragment: Fragment() {
    var notificationsFragmentListener: NotificationsFragmentListener? = null
    var llAds: LinearLayout? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationsAdapter
//    private var notificationBroadcastReceiver: NotificationBroadcastReceiver? = null
    var notificationsInfoManager: NotificationsInfoManager? = null
    private var progressBarLoading: ProgressBar? = null
    private var textViewLoading: TextView? = null
    private var linearLayoutLoading: LinearLayout? = null
    private val notificationRepo = NotificationsRepo(MyApplication.context)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        notificationsFragmentListener = context as NotificationsFragmentListener
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_notifications, container, false)
        if (this.context == null) {
            return v
        }
        notificationsInfoManager = NotificationsInfoManager()
        if (MyApplication.STORE_NOTIFICATIONS) {
            val s = Prefs.getString(Constants.PARAM_NOTIFICATIONS_MANAGER, "")
            if (!s.isEmpty()) {
                notificationsInfoManager = NotificationsInfoManager.fromJson(s)
                notificationsInfoManager!!.notifications.sort()
            }
        }
        initView(v)
        showLoading()
        registerNotificationsReceiver()
        notificationsFragmentListener!!.startNotificationsService()
        return v
    }

    private fun initAds(view: View) {
        val llAds = view.findViewById<LinearLayout>(R.id.llAds)

        val adView = AdView(MyApplication.context)
        adView.adUnitId = getString(R.string.ad_view_unit_id) //todo
        adView.setAdSize(AdSize.SMART_BANNER)


        if (!BuildConfig.IS_PRO) {
            //     MobileAds.initialize(getContext(), initializationStatus -> {
            //     }); todo

            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)

            adView.adListener = object: AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    llAds.addView(adView)
                }
            }
        } else {
            adView.visibility = View.GONE
        }
    }

    private fun showNoNotifications() {
        linearLayoutLoading!!.visibility = View.VISIBLE
        progressBarLoading!!.visibility = View.GONE
        textViewLoading!!.visibility = View.VISIBLE
        textViewLoading!!.text = getString(R.string.no_blocked_notifications)
    }

    private fun showLoading() {
        linearLayoutLoading!!.visibility = View.VISIBLE
        progressBarLoading!!.visibility = View.VISIBLE
        textViewLoading!!.visibility = View.VISIBLE
        textViewLoading!!.text = getString(R.string.loading)
    }

    private fun registerNotificationsReceiver() {
//        Timber.tag("AppInfo").d("registerNotificationsReceiver - NotificationsFragment")
//        notificationBroadcastReceiver = NotificationBroadcastReceiver()
//        val intentFilter = IntentFilter()
//
//        intentFilter.addAction("com.projects.allnotificationblocker.blockthemall")
//        ContextCompat.registerReceiver(
//            requireActivity(),
//            notificationBroadcastReceiver,
//            intentFilter,
//            ContextCompat.RECEIVER_EXPORTED
//        )
    }

    fun unregisterNotificationsReceiver() {
//        Timber.tag("AppInfo").d("unregisterNotificationsReceiver - NotificationsFragment")
//        if (notificationBroadcastReceiver != null) {
//            requireActivity().unregisterReceiver(notificationBroadcastReceiver)
//            notificationBroadcastReceiver = null
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //  unregisterNotificationsReceiver()
    }

    private fun hideLoading() {
        linearLayoutLoading!!.visibility = View.GONE
    }

    private fun initView(itemView: View) {
        initAds(itemView)
        progressBarLoading = itemView.findViewById<ProgressBar>(R.id.progress_bar_loading)
        textViewLoading = itemView.findViewById<TextView>(R.id.text_view_loading)
        linearLayoutLoading = itemView.findViewById<LinearLayout>(R.id.linear_layout_loading)
        llAds = itemView.findViewById<LinearLayout>(R.id.llAds)

        recyclerView = itemView.findViewById<RecyclerView>(R.id.recycler_view)
        val editTextSearch = itemView.findViewById<EditText>(R.id.edit_text_search)

        editTextSearch.addTextChangedListener(object: TextWatcher {
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


        setAdapter()

    }

    fun refreshAd(adView: AdView?) {
        llAds!!.addView(adView)
    }

    fun clearNotifications() {
        val dialog = ConfirmDialog(
            requireActivity(),
            getString(R.string.are_you_sure_clear_notifications)
        )
        dialog.show()
        dialog.setCancelable(true)
        dialog.setOnDismissListener(DialogInterface.OnDismissListener { dialogInterface: DialogInterface? ->
            if (!dialog.result) {
                return@OnDismissListener
            }
            notificationsFragmentListener!!.clearNotifications()//todo check what dose this do
            notificationsInfoManager!!.clearNotifications()
            lifecycleScope.launch {
                NotificationsRepo(MyApplication.context).deleteAllRecords()
                withContext(Dispatchers.Main) {
                    adapter!!.notifyDataSetChanged()
                }
            }
            Prefs.putString(Constants.PARAM_NOTIFICATIONS_MANAGER, "")

        })
    }

    fun removeNotification(myNotification: NotificationInfo?) {
        notificationsInfoManager!!.notifications.remove(myNotification)
        if (MyApplication.STORE_NOTIFICATIONS) {
            Prefs.putString(
                Constants.PARAM_NOTIFICATIONS_MANAGER,
                notificationsInfoManager!!.toJson()
            )
        }
        adapter!!.notifyDataSetChanged()

    }




    private fun setAdapter() {
        if (notificationsInfoManager == null) {
            Timber.tag("Error").e(NullPointerException("notification manager is null"))
            return
        }
        val notificationsLive = notificationRepo.getAllRecordsLive()
        lifecycleScope.launch(Dispatchers.IO) {
            adapter = NotificationsAdapter(requireActivity(), notificationRepo.getAllRecords())
            val layoutManager = LinearLayoutManager(activity)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
        }
        notificationsLive.observe(viewLifecycleOwner) {
            if (recyclerView.adapter == null) {
                return@observe
            }
            lifecycleScope.launch(Dispatchers.Main) {
                adapter.notifications = it
                adapter.notifyDataSetChanged()//todo optimize
                if (it.isNotEmpty()) {
                    hideLoading()
                } else {
                    showNoNotifications()
                }
            }
        }


    }


    interface NotificationsFragmentListener {
        fun clearNotifications()

        fun startNotificationsService()
    }


    companion object {
        fun onReceive(intent: Intent) {

            val myNotification = NotificationInfo.createFromIntent(intent)

            val shouldMuteForNotification = myNotification.isBlocked || myNotification.isSocialAppCall
            if (shouldMuteForNotification) {
                AudioSilencer.pulseMute()
            }
            if (myNotification.isSocialAppCallEnd) {
                AudioSilencer.releasePulseImmediately()
            }
            when (myNotification.type) {
                Constants.DATA_TYPE_APPLICATION -> {
                    /* added =
                         notificationsInfoManager!!.addAppNotification(
                             requireContext(),
                             myNotification
                         )
                     if (added) {
                         if (MyApplication.STORE_NOTIFICATIONS) {
                             Prefs.putString(
                                 Constants.PARAM_NOTIFICATIONS_MANAGER,
                                 notificationsInfoManager!!.toJson()
                             )
                         }

                         adapter.notifyDataSetChanged()
                     }*/

                    // recyclerView.smoothScrollToPosition(0) todo

                }

            }
            val notificationRepo = MyApplication.notificationRepo
            CoroutineScope(Dispatchers.IO).launch {
                notificationRepo.insert(myNotification)
            }
        }
        fun newInstance(): NotificationsFragment {
            val fragment = NotificationsFragment()
            return fragment
        }
    }
}

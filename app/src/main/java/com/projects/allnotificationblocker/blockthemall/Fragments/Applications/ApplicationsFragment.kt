package com.projects.allnotificationblocker.blockthemall.Fragments.Applications

import android.app.*
import android.content.*
import android.os.*
import android.text.*
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.*
import com.google.android.gms.ads.*
import com.projects.allnotificationblocker.blockthemall.*
import com.projects.allnotificationblocker.blockthemall.Activities.Rules.*
import com.projects.allnotificationblocker.blockthemall.Activities.Rules.RulesManager.Companion.fromJson
import com.projects.allnotificationblocker.blockthemall.Application.*
import com.projects.allnotificationblocker.blockthemall.Fragments.Applications.ApplicationsAdapter.*
import com.projects.allnotificationblocker.blockthemall.R
import com.projects.allnotificationblocker.blockthemall.Utilities.*
import com.projects.allnotificationblocker.blockthemall.domain.*
import kotlinx.coroutines.*
import timber.log.*

/**
 * A simple [Fragment] subclass.
 * Use the [ApplicationsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ApplicationsFragment: Fragment(), ApplicationAdapterListener {
    private val apps: MutableList<AppInfo> = ArrayList<AppInfo>()
    var position: Int = -1
    var listener: ApplicationsFragmentListener? = null
    var llAds: LinearLayout? = null
    private var recyclerView: RecyclerView? = null
    private lateinit var adapter: ApplicationsAdapter
    private var mProgressBarLoading: ProgressBar? = null
    private var mTextViewLoading: TextView? = null
    private var mLinearLayoutLoading: LinearLayout? = null
    private var mode = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ApplicationsFragmentListener) {
            listener = context as ApplicationsFragmentListener
        } else {
            throw RuntimeException(
                context
                    .toString() + " must implement ApplicationsFragmentListener"
            )
        }
    }

    private fun initAds(view: View) {
        val llAds = view.findViewById<LinearLayout>(R.id.llAds)

        val adView = AdView(requireContext())
        adView.adUnitId = getString(R.string.ad_view_unit_id)
        adView.setAdSize(AdSize.SMART_BANNER)


        if (!BuildConfig.IS_PRO) {
            //   MobileAds.initialize(getContext(), initializationStatus -> {
            //   });todo

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

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) mode = requireArguments().getInt("mode")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_applications, container, false)
        initView(v)

        loadApps()

        return v
    }

    /**
     * Refresh apps
     */
    fun refreshApps() {
        Timber.tag("AppInfo").d("refreshApps()")
        if (position != -1) {
            adapter!!.notifyItemChanged(position)
        } else {
            adapter!!.notifyDataSetChanged()
        }
        if (apps.isNotEmpty()) {
            hideLoading()
        } else {
            showNoApps()
        }
    }

    fun refreshApps2() {
        Timber.tag("AppInfo").d("refreshApps()")
        if (!this::adapter.isInitialized) return

        adapter!!.notifyDataSetChanged()

        if (apps.isNotEmpty()) {
            hideLoading()
        } else {
            showNoApps()
        }
    }

    private fun showNoApps() {
        mLinearLayoutLoading!!.visibility = View.VISIBLE
        mProgressBarLoading!!.visibility = View.GONE
        mTextViewLoading!!.visibility = View.VISIBLE
        mTextViewLoading!!.text = getString(R.string.no_apps)
    }

    private fun showLoading() {
        mLinearLayoutLoading!!.visibility = View.VISIBLE
        mProgressBarLoading!!.visibility = View.VISIBLE
        mTextViewLoading!!.visibility = View.VISIBLE
        mTextViewLoading!!.text = getString(R.string.loading)
    }

    private fun hideLoading() {
        mLinearLayoutLoading!!.visibility = View.GONE
    }

    private fun initView(itemView: View) {
        initAds(itemView)
        recyclerView = itemView.findViewById<RecyclerView>(R.id.recycler_view)
        mLinearLayoutLoading = itemView.findViewById<LinearLayout>(R.id.linear_layout_loading)
        val editTextSearch = itemView.findViewById<EditText>(R.id.edit_text_search)
        setAdapter()

        editTextSearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                adapter.userFilter = editable.toString()
                adapter.mApps = adapter.mApps
                adapter.notifyDataSetChanged()
            }
        })

        mProgressBarLoading = itemView.findViewById<ProgressBar>(R.id.progress_bar_loading)
        mTextViewLoading = itemView.findViewById<TextView>(R.id.text_view_loading)
        llAds = itemView.findViewById<LinearLayout>(R.id.llAds)
    }

    private fun setAdapter() {
        adapter = ApplicationsAdapter(this, apps, mode)
        recyclerView!!.setHasFixedSize(true)

        // use a linear layout manager
        recyclerView!!.layoutManager = LinearLayoutManager(activity)

        recyclerView!!.adapter = adapter
    }

    override fun enableRules(appInfo: AppInfo?) {
        Timber.tag("AppInfo").d("ApplicationFragment - enableRules")

        if (appInfo == null) return//todo log
        position = apps.indexOf(appInfo)

        if (!rulesManager!!.hasPermenantRule(appInfo.packageName)) {
            rulesManager!!.addPermanentRule(appInfo.packageName)
        }

        rulesManager!!.enableRule(appInfo.packageName, null)
        rulesManager!!.logAllRules()

        listener!!.refreshHome()
    }

    override fun disableRules(appInfo: AppInfo?) {
        Timber.tag("AppInfo").d("ApplicationFragment - disableRules")
        if (appInfo == null) return//todo log
        position = apps.indexOf(appInfo)
        rulesManager!!.disableRule(appInfo.packageName)
        rulesManager!!.logAllRules()

        listener!!.refreshHome()
    }

    override val list: MutableList<AppInfo>
        get() {
            Timber.tag("AppInfo").d("apps.size: %d", apps.size)
            return apps
        }

    override fun selectApp(appInfo: AppInfo) {
        listener!!.selectApp(appInfo)
    }

    override fun unselectApp(appInfo: AppInfo) {
        listener!!.unselectApp(appInfo)
    }

    override val exceptions: ArrayList<String>
        get() = listener!!.rulesManager!!.exceptions

    private fun sortList() {
        val temp: MutableList<AppInfo?> = ArrayList<AppInfo?>()
        if (mode == Constants.MODE_HOMEPAGE || mode == Constants.MODE_PROFILE) {
            var i = apps.size - 1
            while (i >= 0) {
                val ap = apps[i]
                if (!rulesManager!!.getEnabledRules(ap.packageName).isEmpty()) {
                    temp.add(ap)
                    apps.removeAt(i)
                }
                i--
            }
        } else {
            var i = apps.size - 1
            while (i >= 0) {
                val ap = apps[i]
                if (listener!!.exceptions!!.contains(ap.packageName)) {
                    temp.add(ap)
                    apps.removeAt(i)
                }
                i--
            }
        }

        for (i in temp.indices) {
            apps.add(0, temp[i]!!)
        }
    }


    override val rulesManager: RulesManager?
        get() = listener!!.rulesManager


    override fun customRules(appInfo: AppInfo) {
        val intent = Intent(context, RulesActivity::class.java)
        intent.putExtra(Constants.PARAM_MODE, mode)
        intent.putExtra(Constants.PARAM_PACKAGE_NAME, appInfo.packageName)
        intent.putExtra(Constants.PARAM_DATA_TYPE, Constants.DATA_TYPE_APPLICATION)
        intent.putExtra(Constants.PARAM_RULES_MANAGER, listener!!.rulesManager!!.toJson())
        startActivityForResult(intent, Constants.REQ_CODE_NEW_RULE)
    }

    override fun getMode(): Int {
        return mode
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.tag("AppInfo").d("onActivityResult - ApplicationsFragment")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQ_CODE_NEW_RULE) {
            if (resultCode == Activity.RESULT_OK) {
                val s = data!!.getStringExtra(Constants.PARAM_RULES_MANAGER)
                val rulesManager = fromJson(s)
                rulesManager!!.logAllRules()
                listener!!.rulesManager = rulesManager
                adapter!!.notifyDataSetChanged()
            }
        }
    }

    interface ApplicationsFragmentListener {
        fun selectApp(appInfo: AppInfo)
        fun unselectApp(appInfo: AppInfo)
        val exceptions: ArrayList<String?>?
        var rulesManager: RulesManager?
        fun refreshHome()

    }

    fun loadApps() {
        if (MyApplication.Companion.USE_MY_APPLICATION) {
            apps.clear()
            apps.addAll(MyApplication.appInfos)
            apps.sort<AppInfo>()
            sortList()
            adapter!!.notifyDataSetChanged()
            if (!apps.isEmpty()) {
                hideLoading()
            } else {
                showNoApps()
            }
        } else {
            lifecycleScope.launch {
                showLoading()
                val apps1 = withContext(Dispatchers.IO) {
                    loadAppInfo()
                }
                if (apps1.isEmpty()) {
                    showNoApps()
                } else {
                    Timber.tag("AppInfo").d("apps: %d", apps1.size)
                    apps.clear()
                    apps.addAll(apps1)
                    hideLoading()
                    apps.sort()
                    sortList()
                    adapter!!.notifyDataSetChanged()
                }
            }
        }
    }

    private suspend fun loadAppInfo(): MutableList<AppInfo> {
        val res: MutableList<AppInfo> = mutableListOf()
        val packs = MyApplication.context.packageManager.getInstalledPackages(0)
        for (p in packs) {
            if (MyApplication.context.packageManager.getLaunchIntentForPackage(p.packageName) != null) {
                val newInfo = AppInfo()
                newInfo.appName =
                    p.applicationInfo!!.loadLabel(MyApplication.context.packageManager).toString()
                newInfo.packageName = p.packageName
                res.add(newInfo)
            }
        }
        return res
    }


    companion object {
        fun newInstance(mode: Int): ApplicationsFragment {
            val fragment = ApplicationsFragment()
            val args = Bundle()
            args.putInt("mode", mode)
            fragment.arguments = args
            return fragment
        }
    }
}

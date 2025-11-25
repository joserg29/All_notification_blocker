package com.projects.allnotificationblocker.blockthemall.Activities.Home

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender.SendIntentException
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TimingLogger
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.GONE
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.Tab
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.pixplicity.easyprefs.library.Prefs
import com.projects.allnotificationblocker.blockthemall.Activities.AboutActivity
import com.projects.allnotificationblocker.blockthemall.Activities.Profiles.ProfilesViewModel
import com.projects.allnotificationblocker.blockthemall.Activities.Profiles.SelectProfileActivity
import com.projects.allnotificationblocker.blockthemall.Activities.Rules.RulesActivity
import com.projects.allnotificationblocker.blockthemall.Activities.Rules.RulesManager
import com.projects.allnotificationblocker.blockthemall.Activities.Rules.RulesManager.Companion.fromJson
import com.projects.allnotificationblocker.blockthemall.Activities.Rules.RulesViewModel
import com.projects.allnotificationblocker.blockthemall.Activities.premium.PrefSub
import com.projects.allnotificationblocker.blockthemall.Activities.premium.SubscriptionActivity
import com.projects.allnotificationblocker.blockthemall.BuildConfig
import com.projects.allnotificationblocker.blockthemall.Dialogs.ConfirmDialog
import com.projects.allnotificationblocker.blockthemall.Dialogs.ProfileNameDialog.ProfileNameDialogListener
import com.projects.allnotificationblocker.blockthemall.Fragments.Applications.ApplicationsFragment.ApplicationsFragmentListener
import com.projects.allnotificationblocker.blockthemall.Fragments.Notifications.MyNotListenerService
import com.projects.allnotificationblocker.blockthemall.Fragments.Notifications.NotificationsAdapter.NotificationsAdapterListener
import com.projects.allnotificationblocker.blockthemall.Fragments.Notifications.NotificationsFragment.NotificationsFragmentListener
import com.projects.allnotificationblocker.blockthemall.R
import com.projects.allnotificationblocker.blockthemall.Utilities.Constants
import com.projects.allnotificationblocker.blockthemall.Utilities.Constants.RULE_BLOCK_ALL
import com.projects.allnotificationblocker.blockthemall.Utilities.NotificationsUtil.myPackageName
import com.projects.allnotificationblocker.blockthemall.Utilities.Util.enableEdgeToEdge16
import com.projects.allnotificationblocker.blockthemall.Utilities.Util.finishProfile
import com.projects.allnotificationblocker.blockthemall.Utilities.Util.loadRulesManager
import com.projects.allnotificationblocker.blockthemall.Utilities.Util.saveRulesManager
import com.projects.allnotificationblocker.blockthemall.Utilities.Util.startProfile
import com.projects.allnotificationblocker.blockthemall.data.db.converter.ScheduleConverters
import com.projects.allnotificationblocker.blockthemall.data.db.entities.NotificationInfo
import com.projects.allnotificationblocker.blockthemall.data.db.entities.Profile
import com.projects.allnotificationblocker.blockthemall.domain.AppInfo
import timber.log.Timber
import kotlin.system.exitProcess

class HomeActivity : AppCompatActivity(), View.OnClickListener,
    ProfileNameDialogListener, ApplicationsFragmentListener, NotificationsAdapterListener,
    CompoundButton.OnCheckedChangeListener, NotificationsFragmentListener {
    private val profiles = ArrayList<Profile?>()
    private val AdTag = "AD"
    private val mInterval = 2000 // 5 seconds by default, can be changed later
    override var rulesManager: RulesManager? = null
    var selectedProfile: Profile? = null
    var originalWidth: Int = 0
    var imageViewContext: ImageButton? = null
    var linearLayout: LinearLayout? = null
    var linearLayout2: LinearLayout? = null
    var isResized: Boolean = false
    lateinit var tabs: TabLayout
    private var appUpdateManager: AppUpdateManager? = null
    private var mRewardedAd: RewardedInterstitialAd? = null
    private var mInterstitialAd: InterstitialAd? = null
    private var textViewBlockAllTitle: TextView? = null
    private lateinit var switchBlock: SwitchCompat
    private var mImageButtonMore: ImageButton? = null
    private var sectionsPagerAdapter: SectionsPagerAdapter? = null
    private val viewModel: RulesViewModel? = null
    private var textViewBlockAllStatus: TextView? = null
    private var notificationBarReceiver: BroadcastReceiver? = null
    private var mReceiver: BroadcastReceiver? = null
    private lateinit var profilesViewModel: ProfilesViewModel
    private var selectedProfileName: String? = ""
    private var mViewClearNotificationsText: TextView? = null
    private var mViewProfilesText: TextView? = null
    private var mViewResetRulesText: TextView? = null
    private var mViewExitText: TextView? = null

    private var mRL: RelativeLayout? = null
    private var mAppBarLayout: AppBarLayout? = null
    private var mLL2: LinearLayout? = null
    private lateinit var mHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        enableEdgeToEdge16(findViewById(R.id.main))
        rulesManager = loadRulesManager()
        initView()
        mHandler = Handler()
        startRepeatingTask()

        //initFirstRun();
        profilesViewModel =
            ViewModelProviders.of(this).get<ProfilesViewModel>(ProfilesViewModel::class.java)
        profilesViewModel.allRecords
            .observe(this, Observer { records: MutableList<Profile> ->
                profiles.clear()
                profiles.addAll(records)
                applySavedProfile()
            })
        checkForUpdate()

    }

    private fun checkForUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager!!.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener(OnSuccessListener { appUpdateInfo: AppUpdateInfo? ->
            if (appUpdateInfo!!.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                try {
                    appUpdateManager!!.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        UPDATE_REQUEST_CODE
                    )
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun applySavedProfile() {
        Timber.tag("AppInfo").d("applySavedProfile")
        selectedProfileName = Prefs.getString(Constants.PARAM_SELECTED_PROFILE_NAME, "")
        Timber.tag("AppInfo").d("Saved profile name: %s", selectedProfileName)
        if (!selectedProfileName!!.isEmpty()) {
            Timber.tag("AppInfo").d("Applying the saved profile")

            selectedProfile = getProfile(selectedProfileName)
            if (selectedProfile != null) {
                Timber.tag("AppInfo").d("selectedProfile: %s", selectedProfile!!.name)
                //rulesManager.stopAllTimers(getApplicationContext());
                rulesManager = fromJson(selectedProfile!!.rules)
                saveRulesManager(rulesManager!!)
                //rulesManager.startAllTimers(getApplicationContext());
                rulesManager!!.logAllRules()
            } else {
                Timber.tag("AppInfo").d("selectedProfile = null")
            }
        }

        refreshViews(true)
    }

    private fun initView() {
        sectionsPagerAdapter =
            SectionsPagerAdapter(this, supportFragmentManager, Constants.MODE_HOMEPAGE)
        val viewPager = findViewById<ViewPager>(R.id.view_pager)
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = sectionsPagerAdapter
        tabs = findViewById<TabLayout>(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        // Iterate over all tabs and set the custom view
        for (i in 0..<tabs.tabCount) {
            tabs.getTabAt(i)!!.customView = sectionsPagerAdapter!!.getTabView(i)
        }

        sectionsPagerAdapter!!.updateBackground(tabs.getTabAt(0)!!, 0, 0)


        tabs.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: Tab?) {
                // Iterate over all tabs and set the custom view
                for (i in 0..<tabs.tabCount) {
                    val tab1 = tabs.getTabAt(i)

                    sectionsPagerAdapter!!.updateBackground(
                        tab1!!,
                        i,
                        tabs.selectedTabPosition
                    )
                }
            }

            override fun onTabUnselected(tab: Tab?) {
            }

            override fun onTabReselected(tab: Tab?) {
            }
        })
        textViewBlockAllStatus = findViewById<TextView?>(R.id.text_view_block_all_status)
        textViewBlockAllTitle = findViewById<TextView>(R.id.block_all_notifications_text_view)
        switchBlock = findViewById<SwitchCompat>(R.id.block_all_notifications_switch)
        mImageButtonMore = findViewById<ImageButton>(R.id.image_button_more)
        mImageButtonMore!!.setOnClickListener(this)
        setMoreButtonStatus(mImageButtonMore!!, false)
        val mTestButton = findViewById<Button>(R.id.button_test)
        mTestButton.setOnClickListener(this)

        //        tag("AppInfo").d("Initializing ViewModel for Rules");
//        viewModel = ViewModelProviders.of(this).get(RulesViewModel.class);
//
//        tag("AppInfo").d("Initializing ViewModel for Profiles");
//        profilesViewModel = ViewModelProviders.of(this).get(ProfilesViewModel.class);
        linearLayout = findViewById<LinearLayout?>(R.id.linear_layout_top1)
        linearLayout2 = findViewById<LinearLayout>(R.id.linear_layout_top2)

        imageViewContext = findViewById<ImageButton>(R.id.image_button_context)
        imageViewContext!!.setOnClickListener(View.OnClickListener { view: View? ->
            //init the wrapper with style
            val popup = this.popupMenu
            //displaying the popup
            popup.show()
        })

        switchBlock.setOnCheckedChangeListener(this)

        mViewClearNotificationsText = findViewById<TextView>(R.id.text_view_clear_notifications)
        mViewClearNotificationsText!!.setOnClickListener(this)
        mViewProfilesText = findViewById<TextView>(R.id.text_view_profiles)
        mViewProfilesText!!.setOnClickListener(this)
        mViewResetRulesText = findViewById<TextView>(R.id.text_view_reset_rules)
        mViewResetRulesText!!.setOnClickListener(this)
        mViewExitText = findViewById<TextView>(R.id.text_view_exit)
        mViewExitText!!.setOnClickListener(this)

        mRL = findViewById<RelativeLayout?>(R.id.RL)
        mAppBarLayout = findViewById<AppBarLayout?>(R.id.appBarLayout)
        mLL2 = findViewById<LinearLayout?>(R.id.LL2)

        //initAds();
    }

    private val popupMenu: PopupMenu
        get() {
            val wrapper: Context =
                ContextThemeWrapper(this@HomeActivity, R.style.PopupMenu)

            //creating a popup menu
            val popup =
                PopupMenu(wrapper, imageViewContext)
            //inflating menu from xml resource
            popup.inflate(R.menu.mainmenu)
            //adding click listener
            popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->
                val itemId = item!!.itemId
                if (itemId == R.id.menu_item_clear) {
                    sectionsPagerAdapter!!.notificationsFragment!!.clearNotifications()
                    // TODO:TESTING
                    MobileAds.openAdInspector(this) { error ->
                        // Error will be non-null if ad inspector closed due to an error.
                    }
                } else if (itemId == R.id.menu_item_apply_profile) {
                    val intent =
                        Intent(applicationContext, SelectProfileActivity::class.java)
                    startActivityForResult(
                        intent,
                        Constants.REQ_CODE_SELECT_PROFILE
                    )
                } else if (itemId == R.id.menu_item_exit) {
                    confirmExit()
                } else if (itemId == R.id.menu_item_disable_profile) {
                    disableProfile()
                } else if (itemId == R.id.menu_item_privacy_policy) {
                    sendToPrivacyPolicy()
                } else if (itemId == R.id.menu_item_about) {
                    val intent = Intent(applicationContext, AboutActivity::class.java)
                    startActivity(intent)
                }
                false
            })
            return popup
        }

    private fun sendToPrivacyPolicy() {
        val privacyIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://allnotificationblocker.com/privacypolicy.html")
        )
        startActivity(privacyIntent)
    }

    private fun initAds() {
        val adView = AdView(this@HomeActivity)
        adView.adUnitId = getString(R.string.ad_view_unit_id)
        adView.setAdSize(AdSize.SMART_BANNER)


        if (!BuildConfig.IS_PRO) {
            val adRequest = AdRequest.Builder().build()

            //MobileAds.initialize(HomeActivity.this,
            //        initializationStatus -> tag("AppInfo").d("onInitializationComplete"));
//todo
            adView.loadAd(adRequest)
            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Timber.tag("AppInfo").d("onAdLoaded")
                    super.onAdLoaded()
                    //sectionsPagerAdapter.getApplicationsFragment().refreshAd(adView);
                    sectionsPagerAdapter!!.notificationsFragment!!.refreshAd(adView)
                }
            }
        } else {
            adView.visibility = GONE
        }
    }


    private fun checkAndShowSubscriptionDialog(context: Context) {
//        if (PrefSub.isPremium(context)) return // Paid user, skip everything

        val lastTime = PrefSub.getLastDialogTime(context)
        val now = System.currentTimeMillis()
        val twentyFourHours = 24 * 60 * 60 * 1000L

        // TODO:TESTING
        if (true) {
//        if (now - lastTime >= twentyFourHours) {
            Log.d("AdTimerCheck", "✅ 24 hours passed — showing subscribe/ad dialog.")
            preloadAdAndShowRewardedDialog(this)
//            preloadAdAndShowDialog(this)
        } else {
            val hoursLeft = (twentyFourHours - (now - lastTime)) / (1000 * 60 * 60)
            Log.d("AdTimerCheck", "⏳ Not yet — $hoursLeft hours remaining before next dialog.")
        }

    }

    private fun preloadAdAndShowRewardedDialog(context: Context) {
        val adId = if (BuildConfig.DEBUG)
//        val adId = if (BuildConfig.DEBUG)
            context.getString(R.string.ad_rewarded_debug)
        else
            context.getString(R.string.ad_rewarded)

        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(context, adId, adRequest, object : RewardedInterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedInterstitialAd) {
                super.onAdLoaded(ad)
                Log.d("RewardedAd", "onAdLoaded")
                mRewardedAd = ad
                showSubscribeOrWatchAdDialog(context)
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                mRewardedAd = null
                Log.e("RewardedAd", "🔎 Full response: ${loadAdError.responseInfo}")
                PrefSub.saveFailedAdTime(context) // retry after 6 hours
            }
        })
    }

    private fun preloadAdAndShowDialog(context: Context) {
        val adId = /*if (BuildConfig.DEBUG)
            "ca-app-pub-2635111446108281/2390737535"
        else*/
            context.getString(R.string.ad_fullscreen)

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, adId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d("InterstitialAd", "onAdLoaded")
                mInterstitialAd = interstitialAd
                showSubscribeOrWatchAdDialog(context) // show dialog once ad is ready
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                mInterstitialAd = null

                Log.e("InterstitialAd", "🔎 Full response: ${loadAdError.responseInfo}")
                PrefSub.saveFailedAdTime(context) // retry after 6 hours
            }
        })
    }

    private fun showSubscribeOrWatchAdDialog(context: Context) {
        val dialog = AlertDialog.Builder(context)
            .setCancelable(false)
            .setTitle("Upgrade to Premium or Watch an Ad")
            .setMessage(
                "You are currently using the free version. " +
                        "Subscribe to unlock premium features or watch a short ad to continue using the app. " +
                        "Watching ads helps us keep the app free and improve your experience."
            )
            .create()

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Subscribe") { _, _ ->
            dialog.dismiss()
            context.startActivity(Intent(context, SubscriptionActivity::class.java))
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Watch Ad") { _, _ ->
            dialog.dismiss()
            if (mRewardedAd != null) {
                mRewardedAd?.show(context as Activity) {
                    PrefSub.saveDialogTime(context) // Start 24-hour timer
                }
                Log.d("SubscriptionDialog", "🎬 Ad shown successfully, dialog dismissed.")
            } else {
                PrefSub.saveFailedAdTime(context) // Mark failed attempt
                Log.e("SubscriptionDialog", "⚠️ Ad not ready — failed ad time saved.")
            }
        }

        dialog.show()
    }


    /*

        private fun checkAndShowSubscriptionDialog(context: Context) {
            // Paid user? exit
            if (PrefSub.isPremium(context)) return

            val lastTime = PrefSub.getLastDialogTime(context)
            val now = System.currentTimeMillis()
            val twentyFourHours = 24 * 60 * 60 * 1000L

            if (now - lastTime >= twentyFourHours) {
                // Preload ad in background
                preloadInterstitialAd(context)

                // Show dialog
                showSubscribeOrWatchAdDialog(context)

            }
        }

        private fun preloadInterstitialAd(context: Context) {
            val adId = if (BuildConfig.DEBUG)
                "ca-app-pub-3940256099942544/1033173712"
            else
                context.getString(R.string.ad_fullscreen)

            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(context, adId, adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    Timber.d("Interstitial Ad preloaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    mInterstitialAd = null
                    Timber.e("Failed to preload Ad: $loadAdError")
                }
            })
        }

        private fun showSubscribeOrWatchAdDialog(context: Context) {
            val dialog = AlertDialog.Builder(context)
                .setTitle("Upgrade to Premium or Watch an Ad")
                .setMessage(
                    "You are currently using the free version of the app. " +
                            "To unlock all premium features and enjoy an ad-free experience, " +
                            "consider subscribing to our premium plan. " +
                            "Alternatively, you can watch a short ad to continue using the app's features for free. " +
                            "This will help us keep the app running and continuously improve your experience."
                )
                .setCancelable(true)
                .setPositiveButton("Subscribe") { _, _ ->
                    // Open subscription screen
                    context.startActivity(Intent(context, SubscriptionActivity::class.java))
                }
                .setNegativeButton("Watch Ad") { _, _ ->
                    // Show the ad if it's loaded
                    if (mInterstitialAd != null) {
                        mInterstitialAd?.show(context as Activity)
                        // Save the timestamp
                        PrefSub.saveDialogTime(context)
                    } else {
                        Toast.makeText(context, "Ad is not ready yet. Please try again shortly.", Toast.LENGTH_SHORT).show()
                    }
                }
                .create()

            dialog.show()
        }

    */


    private fun checkProfileChanged(): String {
        val timings = TimingLogger("AppInfo", "checkProfileChanged()")

        Timber.tag("AppInfo").d("checkProfileChanged")
        val s = findCurrentProfileName()
        timings.addSplit("String s = findCurrentProfileName();")

        Timber.tag("AppInfo").d("s: %s", s)
        if (s.isEmpty()) {
            Prefs.putString(Constants.PARAM_SELECTED_PROFILE_NAME, "")
            timings.addSplit("Prefs.putString(Constants.PARAM_SELECTED_PROFILE_NAME);")
            saveRulesManager(rulesManager!!)
            timings.addSplit("Util.saveRulesManager(getRulesManager());")
        } else {
            Prefs.putString(Constants.PARAM_SELECTED_PROFILE_NAME, s)
            timings.addSplit("Prefs.putString(Constants.PARAM_SELECTED_PROFILE_NAME);")
        }

        timings.dumpToLog()

        return s
    }

    private fun refreshViews(b: Boolean) {
        val timings = TimingLogger("AppInfo", "refreshViews")

        Timber.tag("AppInfo").d("refreshViews()")
        Timber.tag("AppInfo").d("Checking if the current profile is changed by user")
        selectedProfileName = checkProfileChanged()
        timings.addSplit("selectedProfileName = checkProfileChanged();")

        Timber.tag("AppInfo").d("selectedProfileName: %s", selectedProfileName)
        if (selectedProfileName!!.isEmpty()) {
            Timber.tag("AppInfo").d("Yes, it's changed")
            // getSupportActionBar().setTitle(getString(R.string.app_name));
        } else {
            Timber.tag("AppInfo").d("No, it's not changed")
            // getSupportActionBar().setTitle(getString(R.string.app_name) + " (" + selectedProfileName + ")");
        }

        refreshBlockAllSwitch()
        if (b) {
            timings.addSplit("refreshBlockAllSwitch()")
        }

        refreshBlockingStatus()
        timings.addSplit("refreshBlockingStatus()")
        sectionsPagerAdapter!!.applicationsFragment!!.position = -1
        //sectionsPagerAdapter!!.contactsFragment!!.position = -1
        sectionsPagerAdapter!!.applicationsFragment!!.refreshApps2()
        timings.addSplit("sectionsPagerAdapter.getApplicationsFragment().refreshApps2(rulesManager)")
        //sectionsPagerAdapter!!.contactsFragment!!.refreshContacts2(rulesManager)
        timings.addSplit("sectionsPagerAdapter.getContactsFragment().refreshContacts2(rulesManager);")
        timings.dumpToLog()
    }

    override fun refreshHome() {
        Timber.tag("AppInfo").d("refreshHome()")
        Timber.tag("AppInfo").d("Checking if the current profile is changed by user")
        selectedProfileName = checkProfileChanged()
        Timber.tag("AppInfo").d("selectedProfileName: %s", selectedProfileName)
        refreshBlockAllSwitch()
        refreshBlockingStatus()
        // Refresh applications fragment to update all switch states
        sectionsPagerAdapter?.applicationsFragment?.let {
            it.position = -1
            it.refreshApps2()
        }
    }

    fun findCurrentProfileName(): String {
        Timber.tag("AppInfo").d("findCurrentProfileName")
        Timber.tag("AppInfo").d("rulesManager.toJson(): %s", rulesManager!!.toJson())
        for (i in profiles.indices) {
            profiles[i]!!.logProfile()
            if (profiles[i]!!.rules == rulesManager!!.toJson()) {
                return profiles[i]!!.name
            }
        }
        return ""
    }

    fun getProfile(name: String?): Profile? {
        Timber.tag("AppInfo").d("getProfile")
        Timber.tag("AppInfo").d("name: %s", name)
        Timber.tag("AppInfo").d("profiles size: %d", profiles.size)
        for (i in profiles.indices) {
            profiles[i]!!.logProfile()
            if (profiles[i]!!.name == name) {
                return profiles[i]
            }
        }

        return null
    }

    override fun onStart() {
        super.onStart()
        registerAllReceivers()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startActivity(startMain)
    }


    override fun onResume() {
        super.onResume()
        checkAndShowSubscriptionDialog(this)
        // Resume update if it was interrupted
//        if (MyApplication.lastShownAd + Constants.AD_INTERVAL <= System.currentTimeMillis()) {
//            loadFullScreenAd()
//            MyApplication.lastShownAd = System.currentTimeMillis()
//        }
        appUpdateManager!!
            .appUpdateInfo
            .addOnSuccessListener(OnSuccessListener { appUpdateInfo: AppUpdateInfo? ->
                if (appUpdateInfo!!.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    try {
                        appUpdateManager!!.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            UPDATE_REQUEST_CODE
                        )
                    } catch (e: SendIntentException) {
                        e.printStackTrace()
                    }
                }
            })
    }

    private fun registerAllReceivers() {
        Timber.tag("AppInfo").d("registerAllReceivers")
        RegisterAlarmBroadcast()
        RegisterBarNotificationReceiver()
    }

    private fun RegisterBarNotificationReceiver() {
        notificationBarReceiver = object : BroadcastReceiver() {
            // todo check why this is not registered
            override fun onReceive(context: Context, intent: Intent) {
                Timber.tag("AppInfo").d("###### NotificationBarReceiver #######")

                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.cancel(1)

                val action = intent.getIntExtra("action", -1)
                Timber.tag("AppInfo").d("action = %d", action)

                when (action) {
                    Constants.BLOCK_ALL -> {
                        Timber.tag("AppInfo").d("BLOCK_ALL received")
                        //MyApplication.rulesManager.enableRule(Constants.RULE_BLOCK_ALL, rulesViewModel);
                        this@HomeActivity.finish()
                    }

                    Constants.UNBLOCK_ALL -> Timber.tag("AppInfo").d("DISABLE_BLOCK_ALL received")
                }
            }
        }

        val filter: IntentFilter = IntentFilter(myPackageName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(mReceiver, filter, RECEIVER_EXPORTED)//todo check
        }
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, b: Boolean) {
        Timber.tag("AppInfo").d("onCheckedChanged - b: %s", b)
        val timings = TimingLogger("AppInfo", "methodA")
        setMoreButtonStatus(mImageButtonMore!!, b)
        timings.addSplit("setMoreButtonStatus(mImageButtonMore, b);")
        if (b) {
            if (!rulesManager!!.hasPermenantRule(RULE_BLOCK_ALL)) {
                timings.addSplit("!rulesManager.hasPermenantRule(RULE_BLOCK_ALL)")
                rulesManager!!.addPermanentRule(RULE_BLOCK_ALL)
                timings.addSplit("rulesManager.addPermanentRule(RULE_BLOCK_ALL, viewModel);")
            }
            rulesManager!!.enableRule(RULE_BLOCK_ALL, viewModel)
            timings.addSplit("rulesManager.enableRule(RULE_BLOCK_ALL, viewModel);")
        } else {
            rulesManager!!.disableRule(RULE_BLOCK_ALL)
            timings.addSplit("rulesManager.disableRule(RULE_BLOCK_ALL, viewModel);")
        }


        saveRulesManager(rulesManager!!)
        timings.addSplit("Util.saveRulesManager(rulesManager);")
        
        // Ensure service is running and trigger immediate cancellation
        ensureServiceRunningAndCancelNotifications()
        timings.addSplit("ensureServiceRunningAndCancelNotifications();")
        
        // Refresh all views including applications fragment to update switch states
        refreshViews(true)

        timings.addSplit("refreshViews();")

        timings.dumpToLog()
    }
    
    private fun ensureServiceRunningAndCancelNotifications() {
        try {
            // Ensure service is running first
            if (!MyNotListenerService.isServiceRunning) {
                Timber.tag(TAG).d("Service not running, starting it...")
                MyNotListenerService.startService(applicationContext, MyNotListenerService.Actions.Enable)
                // Give it a moment to start, then trigger cancellation
                Handler(Looper.getMainLooper()).postDelayed({
                    MyNotListenerService.triggerImmediateCancellation(applicationContext)
                }, 100)
            } else {
                // Service is running, trigger immediate cancellation
                MyNotListenerService.triggerImmediateCancellation(applicationContext)
            }
            Timber.tag(TAG).d("Triggered immediate notification cancellation")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error ensuring service running and cancelling notifications")
        }
    }

    override fun startNotificationsService() {
        MyNotListenerService.startService(applicationContext, MyNotListenerService.Actions.Enable)
    }

    override fun clearNotifications() {
        MyNotListenerService.stopService(applicationContext)
        MyNotListenerService.startService(
            applicationContext,
            MyNotListenerService.Actions.DeleteAll
        )
    }

    private fun refreshBlockAllSwitch() {
        switchBlock.setOnCheckedChangeListener(null)
        if (rulesManager!!.isBlockAllEnabled
            || rulesManager!!.hasCustomEnabledValidRules(RULE_BLOCK_ALL) == true
        ) {
            switchBlock.isChecked = true
            setMoreButtonStatus(mImageButtonMore!!, true)
            textViewBlockAllTitle!!.setText(R.string.unblock_all_notifications_calls)
        } else {
            switchBlock.isChecked = false
            setMoreButtonStatus(mImageButtonMore!!, false)
            textViewBlockAllTitle!!.setText(R.string.block_all_notifications_calls)
        }


        switchBlock.setOnCheckedChangeListener(this)
    }

    private fun refreshBlockingStatus() {
        startProfile("refreshBlockingStatus")
        Timber.tag("AppInfo").d("###### refreshBlockingStatus")


        refreshBlockAllSwitch()


        finishProfile("refreshBlockingStatus", 2)
    }

    private fun setMoreButtonStatus(imageButton: ImageButton, value: Boolean) {
        if (value) {
            imageButton.setEnabled(true)
            imageButton.setColorFilter(resources.getColor(R.color.colorMoreEnabled))
        } else {
            imageButton.setEnabled(false)
            imageButton.setColorFilter(resources.getColor(R.color.colorAccent))
        }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.image_button_more) { //creating a popup menu
            //init the wrapper with style
            val wrapper: Context = ContextThemeWrapper(this@HomeActivity, R.style.PopupMenu)

            //creating a popup menu
            val popup = PopupMenu(wrapper, mImageButtonMore)
            //inflating menu from xml resource
            popup.inflate(R.menu.apps_menu_block_all)
            //adding click listener
            popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->
                if (item!!.itemId == R.id.custom_rule) { //handle menu1 click
                    val intent = Intent(applicationContext, RulesActivity::class.java)
                    intent.putExtra(Constants.PARAM_PACKAGE_NAME, RULE_BLOCK_ALL)
                    intent.putExtra(Constants.PARAM_MODE, Constants.MODE_HOMEPAGE)

                    intent.putExtra(Constants.PARAM_DATA_TYPE, Constants.DATA_TYPE_BLOCK_ALL)
                    intent.putExtra(Constants.PARAM_RULES_MANAGER, rulesManager!!.toJson())
                    startActivityForResult(intent, Constants.REQ_CODE_NEW_RULE)
                }
                if (item.itemId == R.id.exceptions) { //handle menu1 click
                    val intent = Intent(applicationContext, ExceptionsActivity::class.java)
                    intent.putExtra(Constants.PARAM_SELECTED_PROFILE, "")
                    startActivityForResult(intent, Constants.REQ_CODE_EXCEPTIONS)
                }
                false
            })
            //displaying the popup
            popup.show()
        }


        if (v.id == R.id.text_view_clear_notifications) {
            sectionsPagerAdapter!!.notificationsFragment!!.clearNotifications()
        }

        if (v.id == R.id.text_view_profiles) {
            val intent = Intent(applicationContext, SelectProfileActivity::class.java)
            startActivityForResult(intent, Constants.REQ_CODE_SELECT_PROFILE)
        }

        if (v.id == R.id.text_view_exit) {
            confirmExit()
        }

        if (v.id == R.id.text_view_reset_rules) {
            disableProfile()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.tag("AppInfo").d("onActivityResult")
        Timber.tag("AppInfo").d("requestCode = %d", requestCode)
        Timber.tag("AppInfo").d("resultCode = %d", resultCode)

        if (requestCode == Constants.REQ_CODE_EXCEPTIONS) {
            if (resultCode == RESULT_OK) {
                val exceptions = data!!.getStringArrayListExtra(Constants.PARAM_EXCEPTIONS)
                Timber.tag("AppInfo").d("exceptions.size: %d", exceptions!!.size)
                for (i in exceptions.indices) {
                    Timber.tag("AppInfo").d("Exception: %s", exceptions.get(i))
                }
                rulesManager!!.exceptions = exceptions
                saveRulesManager(rulesManager!!)
                refreshViews(true)
                rulesManager!!.logAllRules()
            }
        }
        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "App update is required to continue.", Toast.LENGTH_LONG)
                    .show()
            }
        }


        if (requestCode == Constants.REQ_CODE_SELECT_PROFILE) {
            if (resultCode == RESULT_OK) {
                checkNotNull(data)
                selectedProfileName = data.getStringExtra(Constants.PARAM_SELECTED_PROFILE_NAME)
                Timber.tag(TAG).d("@@@ selectedProfileName: %s", selectedProfileName)
                val profileString = data.getStringExtra(Constants.PARAM_SELECTED_PROFILE)
                val p = Profile.fromJson(profileString)
                // rulesManager.stopAllTimers(getApplicationContext());
                rulesManager = fromJson(p.rules)

                Timber.tag(TAG).d("APPLIED PROFILE RULES")
                rulesManager!!.logAllRules(TAG)

                //rulesManager.startAllTimers(getApplicationContext());
                saveRulesManager(rulesManager!!)
                Prefs.putString(Constants.PARAM_SELECTED_PROFILE_NAME, selectedProfileName)
                refreshViews(true)
            }
        }

        if (requestCode == Constants.REQ_CODE_NEW_RULE) {
            if (resultCode == RESULT_OK) {
                val ruleManagerString = data!!.getStringExtra(Constants.PARAM_RULES_MANAGER)
                rulesManager = fromJson(ruleManagerString)
                saveRulesManager(rulesManager!!)
                refreshViews(true)
            }
        }
    }

    private fun RegisterAlarmBroadcast() {
        Timber.i("Going to register Intent.RegisterAlramBroadcast")
        mReceiver = object : BroadcastReceiver() {
            private val TAG = "Alarm Example Receiver"
            override fun onReceive(context: Context?, intent: Intent) {
                Timber.tag(TAG).i("BroadcastReceiver::OnReceive() >>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
                val rule_type = intent.getIntExtra("rule_type", -1)
                val package_name = intent.getStringExtra("package_name")
                val schedule = ScheduleConverters().fromJson(intent.getStringExtra("schedule"))!!
                rulesManager!!.disableRule(package_name, schedule)
                Timber.tag("AppInfo").d("rule_type = %s", rule_type)
                Timber.tag("AppInfo").d("package_name = %s", package_name)
                rulesManager!!.logAllRules()
                refreshViews(true)
            }
        }

        // register the alarm broadcast here
        val filter = IntentFilter("com.techblogon.alarmexample")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mReceiver, filter, RECEIVER_EXPORTED)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = getMenuInflater()
        menuInflater.inflate(R.menu.mainmenu, menu)


        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.menu_item_clear) {
            sectionsPagerAdapter!!.notificationsFragment.clearNotifications()
        } else if (itemId == R.id.menu_item_apply_profile) {
            val intent = Intent(applicationContext, SelectProfileActivity::class.java)
            startActivityForResult(intent, Constants.REQ_CODE_SELECT_PROFILE)
        } else if (itemId == R.id.menu_item_exit) {
            confirmExit()
        } else if (itemId == R.id.menu_item_disable_profile) {
            disableProfile()
        }
        return true
    }

    private fun disableProfile() {
        val dialog = ConfirmDialog(this, getString(R.string.are_you_sure_disable_profile))

        dialog.show()
        dialog.setCancelable(true)
        dialog.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(dialogInterface: DialogInterface?) {
                if (dialog.result) {
                    selectedProfileName = ""
                    Prefs.putString(Constants.PARAM_SELECTED_PROFILE_NAME, selectedProfileName)
                    rulesManager = RulesManager()
                    saveRulesManager(rulesManager!!)
                    refreshViews(true)
                }
            }
        })
    }

    private fun confirmExit() {
        val dialog = ConfirmDialog(this, getString(R.string.are_you_sure_exit))

        dialog.show()
        dialog.setCancelable(true)
        dialog.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(dialogInterface: DialogInterface?) {
                if (dialog.result) {
                    MyNotListenerService.stopService(this@HomeActivity)
                    this@HomeActivity.finish()
                    exitProcess(0)
                }
            }
        })
    }

    override val notificationsList: MutableList<NotificationInfo?>?
        get() = sectionsPagerAdapter!!.notificationsFragment.notificationsInfoManager!!.notifications as MutableList<NotificationInfo?>?

    override fun removeNotification(myNotification: NotificationInfo?) {
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Do something after 5s = 4000ms
                //           sectionsPagerAdapter!!.notificationsFragment.removeNotification(myNotification)
                //           (myNotification)
            }
        }, 2000)
    }

    override fun selectApp(appInfo: AppInfo) {
    }

    override fun unselectApp(appInfo: AppInfo) {
    }

    override val exceptions: java.util.ArrayList<String?>?
        get() = null


    override fun isProfileNameExist(name: String?): Boolean {
        for (i in profiles.indices) {
            if (profiles.get(i)!!.name == name) {
                return true
            }
        }

        return false
    }

    fun startRepeatingTask() {
        mStatusChecker.run()
    }

    fun stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker)
    }

    var mStatusChecker: Runnable = object : Runnable {
        override fun run() {
            try {
                if (rulesManager != null) {
                    val b = rulesManager!!.checkCustomRules()
                    if (b) {
                        rulesManager!!.logAllRules()
                        refreshViews(true)
                    }
                }
            } finally {
                mHandler.postDelayed(mStatusChecker, mInterval.toLong())
            }
        }
    }


    companion object {
        const val TAG = "HomeActivity"
        private const val UPDATE_REQUEST_CODE = 123
        private const val NOTIFICATION_ID = 650300
    }
}
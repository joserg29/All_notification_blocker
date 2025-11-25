package com.projects.allnotificationblocker.blockthemall.Activities.Profiles

import android.content.*
import android.os.*
import android.view.*
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.*
import androidx.cardview.widget.*
import androidx.lifecycle.*
import androidx.viewpager.widget.*
import com.google.android.gms.ads.*
import com.google.android.material.appbar.*
import com.google.android.material.switchmaterial.*
import com.google.android.material.tabs.*
import com.google.android.material.tabs.TabLayout.*
import com.projects.allnotificationblocker.blockthemall.Activities.Home.*
import com.projects.allnotificationblocker.blockthemall.Activities.Rules.*
import com.projects.allnotificationblocker.blockthemall.Activities.Rules.RulesManager.Companion.fromJson
import com.projects.allnotificationblocker.blockthemall.Dialogs.*
import com.projects.allnotificationblocker.blockthemall.Dialogs.ProfileNameDialog.*
import com.projects.allnotificationblocker.blockthemall.Fragments.Applications.ApplicationsFragment.*
import com.projects.allnotificationblocker.blockthemall.Fragments.Notifications.MyNotListenerService
import com.projects.allnotificationblocker.blockthemall.Fragments.Notifications.NotificationsAdapter.*
import com.projects.allnotificationblocker.blockthemall.R
import com.projects.allnotificationblocker.blockthemall.Utilities.*
import com.projects.allnotificationblocker.blockthemall.Utilities.Constants.RULE_BLOCK_ALL
import com.projects.allnotificationblocker.blockthemall.Utilities.Util.enableEdgeToEdge16
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*
import com.projects.allnotificationblocker.blockthemall.domain.*
import timber.log.*

class AddEditProfileActivity: AppCompatActivity(), View.OnClickListener,
    ProfileNameDialogListener, ApplicationsFragmentListener, NotificationsAdapterListener,
    CompoundButton.OnCheckedChangeListener {
    private val profiles = ArrayList<Profile?>()
    override var rulesManager: RulesManager? = RulesManager()
    lateinit var selectedProfile: Profile
    var tabs: TabLayout? = null
    private var textViewBlockAllTitle: TextView? = null
    private var switchBlock: SwitchMaterial? = null
    private var mImageButtonMore: ImageButton? = null
    private var sectionsPagerAdapter: SectionsPagerAdapter? = null
    private var textViewBlockAllStatus: TextView? = null
    private val mReceiver: BroadcastReceiver? = null
    private val mAdView: AdView? = null
    private var profilesViewModel: ProfilesViewModel? = null
    private var selectedProfileName: String? = ""
    private val mCardView3: CardView? = null
    private val mCardView2: CardView? = null
    private var mAppBarLayout: AppBarLayout? = null
    private var mSaveProfileButton: Button? = null
    private var mButtonBackImage: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_profile)
        enableEdgeToEdge16(findViewById(R.id.main))
        initView()

        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //initViewModel();
        val intent = getIntent()
        if (intent != null) {
            selectedProfileName = intent.getStringExtra(Constants.PARAM_SELECTED_PROFILE_NAME)
            val profileString = intent.getStringExtra(Constants.PARAM_SELECTED_PROFILE)
            if (profileString != null) {
                Timber.tag("AppInfo").d("profileString: %s", profileString)
                selectedProfile = Profile.fromJson(profileString)
                rulesManager = fromJson(selectedProfile!!.rules)
                rulesManager!!.logAllRules()
                // getSupportActionBar().setTitle("Edit Profile: " + selectedProfileName);
            } else {
                rulesManager = RulesManager()
                // getSupportActionBar().setTitle("New Profile");
            }
        } else {
            rulesManager = RulesManager()
            // getSupportActionBar().setTitle("New Profile");
        }
        initView()
        refreshViews()
    }

    private fun initView() {
        sectionsPagerAdapter =
            SectionsPagerAdapter(this, supportFragmentManager, Constants.MODE_PROFILE)
        val viewPager = findViewById<ViewPager>(R.id.view_pager)
        viewPager.offscreenPageLimit = 2

        viewPager.adapter = sectionsPagerAdapter
        tabs = findViewById<TabLayout>(R.id.tabs)
        tabs!!.setupWithViewPager(viewPager)


        Timber.tag("AppInfo").d("tabs.getTabCount(): %d", tabs!!.tabCount)

        // Iterate over all tabs and set the custom view
        for (i in 0..<tabs!!.tabCount) {
            val tab = tabs!!.getTabAt(i)
            val v = sectionsPagerAdapter!!.getTabView(i)
            Timber.tag("AppInfo").d("v != null")
            tab!!.customView = v
        }

        sectionsPagerAdapter!!.updateBackground(tabs!!.getTabAt(0)!!, 0, 0)


        tabs!!.addOnTabSelectedListener(object: OnTabSelectedListener {
            override fun onTabSelected(tab: Tab?) {
                // Iterate over all tabs and set the custom view
                for (i in 0..<tabs!!.tabCount) {
                    val tab1 = tabs!!.getTabAt(i)

                    sectionsPagerAdapter!!.updateBackground(
                        tab1!!,
                        i,
                        tabs!!.selectedTabPosition
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
        switchBlock = findViewById<SwitchMaterial>(R.id.block_all_notifications_switch)
        mImageButtonMore = findViewById<ImageButton>(R.id.image_button_more)
        mImageButtonMore!!.setOnClickListener(this)

        setMoreButtonStatus(mImageButtonMore!!, false)

        switchBlock!!.setOnCheckedChangeListener(this)
        val mTestButton = findViewById<Button>(R.id.button_test)
        mTestButton.setOnClickListener(this)


        mAppBarLayout = findViewById<AppBarLayout?>(R.id.appBarLayout)
        mSaveProfileButton = findViewById<Button>(R.id.button_save_profile)
        mSaveProfileButton!!.setOnClickListener(this)


        profilesViewModel =
            ViewModelProviders.of(this).get<ProfilesViewModel>(ProfilesViewModel::class.java)

        mButtonBackImage = findViewById<ImageButton>(R.id.image_button_back)
        mButtonBackImage!!.setOnClickListener(this)
    }

    private fun initViewModel() {
        loadProfiles()
    }


    private fun loadProfiles() {
        Timber.tag("AppInfo").d("Initializing ViewModel for Profiles")
        profilesViewModel =
            ViewModelProviders.of(this).get<ProfilesViewModel>(ProfilesViewModel::class.java)

        Timber.tag("AppInfo").d("Loading profiles")
        profilesViewModel!!.allRecords
            .observe(this, Observer { records: MutableList<Profile> ->
                profiles.clear()
                profiles.addAll(records)

                val intent = getIntent()
                if (intent != null) {
                    selectedProfileName =
                        intent.getStringExtra(Constants.PARAM_SELECTED_PROFILE_NAME)
                    if (selectedProfileName != null) {
                        val p = getProfile(selectedProfileName)
                        if (p != null) {
                            rulesManager = fromJson(p.rules)
                            initView()
                            refreshViews()
                        }
                    } else {
                        rulesManager = RulesManager()
                        initView()
                        refreshViews()
                    }
                } else {
                    rulesManager = RulesManager()
                    initView()
                    refreshViews()
                }
            })
    }

    fun getProfile(name: String?): Profile? {
        for (i in profiles.indices) {
            if (profiles.get(i)!!.name == name) {
                return profiles.get(i)
            }
        }

        return null
    }

    override fun onBackPressed() {
        //Execute your code here
        super.onBackPressed()
        setResult(RESULT_CANCELED)
        finish()
    }

    private fun refreshViews() {
        refreshBlockAllSwitch()
        refreshBlockingStatus()

        sectionsPagerAdapter!!.applicationsFragment!!.refreshApps2()
        //sectionsPagerAdapter!!.contactsFragment!!.refreshContacts2(rulesManager)
    }


    override fun onCheckedChanged(compoundButton: CompoundButton, b: Boolean) {
        Timber.tag("AppInfo").d("onCheckedChanged - b: %s", b)
        setMoreButtonStatus(mImageButtonMore!!, b)

        if (b) {
            if (!rulesManager!!.hasPermenantRule(RULE_BLOCK_ALL)) {
                rulesManager!!.addPermanentRule(RULE_BLOCK_ALL)
            }
            rulesManager!!.enableRule(RULE_BLOCK_ALL, null)
        } else {
            rulesManager!!.disableRule(RULE_BLOCK_ALL)
        }
        
        // Save rules and notify notification service immediately
        Util.saveRulesManager(rulesManager!!)
        ensureServiceRunningAndCancelNotifications()

        refreshViews()
    }
    
    private fun ensureServiceRunningAndCancelNotifications() {
        try {
            // Ensure service is running first
            if (!MyNotListenerService.isServiceRunning) {
                Timber.tag("AppInfo").d("Service not running, starting it...")
                MyNotListenerService.startService(applicationContext, MyNotListenerService.Actions.Enable)
                // Give it a moment to start, then trigger cancellation
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    MyNotListenerService.triggerImmediateCancellation(applicationContext)
                }, 100)
            } else {
                // Service is running, trigger immediate cancellation
                MyNotListenerService.triggerImmediateCancellation(applicationContext)
            }
            Timber.tag("AppInfo").d("Triggered immediate notification cancellation")
        } catch (e: Exception) {
            Timber.tag("AppInfo").e(e, "Error ensuring service running and cancelling notifications")
        }
    }

    private fun refreshBlockAllSwitch() {
        switchBlock!!.setOnCheckedChangeListener(null)
        if (rulesManager!!.isBlockAllEnabled
            || rulesManager!!.hasCustomEnabledValidRules(RULE_BLOCK_ALL) == true
        ) {
            switchBlock!!.isChecked = true
            setMoreButtonStatus(mImageButtonMore!!, true)
            textViewBlockAllTitle!!.setText(R.string.unblock_all_notifications_calls)
        } else {
            switchBlock!!.isChecked = false
            setMoreButtonStatus(mImageButtonMore!!, false)
            textViewBlockAllTitle!!.setText(R.string.block_all_notifications_calls)
        }


        switchBlock!!.setOnCheckedChangeListener(this)
    }

    private fun refreshBlockingStatus() {
        Timber.tag("AppInfo").d("###### refreshBlockingStatus")


    }


    private fun setMoreButtonStatus(imageButton: ImageButton, b: Boolean) {
        if (b) {
            imageButton.setEnabled(true)
            imageButton.setColorFilter(resources.getColor(R.color.colorMoreEnabled))
        } else {
            imageButton.setEnabled(false)
            imageButton.setColorFilter(resources.getColor(R.color.colorDisabled))
        }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.image_button_more) { //creating a popup menu
            //init the wrapper with style
            val wrapper: Context =
                ContextThemeWrapper(this@AddEditProfileActivity, R.style.PopupMenu)

            //creating a popup menu
            val popup = PopupMenu(wrapper, mImageButtonMore)
            //inflating menu from xml resource
            popup.inflate(R.menu.apps_menu_block_all)
            //adding click listener
            popup.setOnMenuItemClickListener(object: PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    if (item.itemId == R.id.custom_rule) { //handle menu1 click
                        val intent = Intent(applicationContext, RulesActivity::class.java)
                        intent.putExtra(Constants.PARAM_PACKAGE_NAME, RULE_BLOCK_ALL)
                        intent.putExtra(Constants.PARAM_DATA_TYPE, Constants.DATA_TYPE_BLOCK_ALL)
                        intent.putExtra(Constants.PARAM_RULES_MANAGER, rulesManager!!.toJson())
                        intent.putExtra(Constants.PARAM_MODE, Constants.MODE_PROFILE)
                        startActivityForResult(intent, Constants.REQ_CODE_NEW_RULE)
                    }

                    if (item.itemId == R.id.exceptions) { //handle menu1 click
                        val intent = Intent(applicationContext, ExceptionsActivity::class.java)

                        Timber.tag("AppInfo").d(
                            "AddEditProfileActivity selectedProfileName: %s",
                            selectedProfileName
                        )

                        if (selectedProfile != null) {
                            Timber.tag("AppInfo").d("p.toJson(): %s", selectedProfile!!.toJson())
                            intent.putExtra(
                                Constants.PARAM_SELECTED_PROFILE,
                                selectedProfile!!.toJson()
                            )
                        }
                        startActivityForResult(intent, Constants.REQ_CODE_EXCEPTIONS)
                    }
                    return false
                }
            })
            //displaying the popup
            popup.show()
        }


        if (v.id == R.id.button_save_profile) {
            saveProfile()
        }

        if (v.id == R.id.image_button_back) {
            onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQ_CODE_EXCEPTIONS) {
            if (resultCode == RESULT_OK) {
                checkNotNull(data)
                val exceptions = data.getStringArrayListExtra(Constants.PARAM_EXCEPTIONS)
                rulesManager!!.exceptions = exceptions!!
                Timber.tag("AppInfo#2").d("Exceptions")
                rulesManager!!.logAllRules(TAG)
            }
        }

        if (requestCode == Constants.REQ_CODE_SELECT_PROFILE) {
            if (resultCode == RESULT_OK) {
                val s = data!!.getStringExtra(Constants.PARAM_RULES_MANAGER)
                rulesManager = fromJson(s)
                refreshViews()
            }
        }

        if (requestCode == Constants.REQ_CODE_NEW_RULE) {
            if (resultCode == RESULT_OK) {
                val ruleManagerString = data!!.getStringExtra(Constants.PARAM_RULES_MANAGER)
                rulesManager = fromJson(ruleManagerString)
                Util.saveRulesManager(rulesManager!!)
                refreshViews()
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { //finish();
            onBackPressed()
        }
        return true
    }

    private fun saveProfile() {
        if (selectedProfileName == null || selectedProfileName!!.isEmpty()) {
            val dialog = ProfileNameDialog(
                this,
                getString(R.string.save_profile)
            )

            dialog.show()
            dialog.setCancelable(true)
            dialog.setOnDismissListener(DialogInterface.OnDismissListener { dialogInterface: DialogInterface? ->
                val profileName = dialog.name
                val p = getProfile(profileName)
                val profileDescription = dialog.description
                if (p == null) {
                    selectedProfileName = profileName
                    //Prefs.putString(Constants.PARAM_SELECTED_PROFILE_NAME, selectedProfileName);
                    Timber.tag("AppInfo").d("Saving new profile: %s", selectedProfileName)
                    rulesManager!!.logAllRules()

                    profilesViewModel!!.insert(
                        Profile(
                            profileName,
                            profileDescription,
                            rulesManager!!.toJson()
                        )
                    )
                    finish()
                } else {
                    updateCurrentProfile()
                }
            })
        } else {
            updateCurrentProfile()
        }
    }

    private fun updateCurrentProfile() {
        val dialog = ConfirmDialog(
            this@AddEditProfileActivity,
            getString(R.string.are_you_sure_overwrite_profile, selectedProfileName)
        )
        dialog.show()
        dialog.setCancelable(true)
        dialog.setOnDismissListener(object: DialogInterface.OnDismissListener {
            override fun onDismiss(dialogInterface: DialogInterface?) {
                if (dialog.result) {
                    //Prefs.putString(Constants.PARAM_SELECTED_PROFILE_NAME, selectedProfileName);
                    selectedProfile!!.rules = rulesManager!!.toJson()
                    profilesViewModel!!.update(selectedProfile)

                    finish()
                }
            }
        })
    }


    override val notificationsList: MutableList<NotificationInfo?>?
        get() = null

    override fun removeNotification(myNotification: NotificationInfo?) {
    }


    override fun selectApp(appInfo: AppInfo) {
    }

    override fun unselectApp(appInfo: AppInfo) {
    }

    override val exceptions: java.util.ArrayList<String?>?
        get() = null


    override fun refreshHome() {
        refreshBlockAllSwitch()
        refreshBlockingStatus()
    }

    override fun isProfileNameExist(name: String?): Boolean {
        for (i in profiles.indices) {
            if (profiles[i]!!.name == name) {
                return true
            }
        }

        return false
    }

    companion object {
        const val TAG = "AddEditProfileActivity"
        private const val NOTIFICATION_ID = 650300
    }
}
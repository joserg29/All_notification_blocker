package com.projects.allnotificationblocker.blockthemall.Activities.Home

import android.content.*
import android.os.*
import android.view.*
import android.widget.*
import androidx.appcompat.app.*
import androidx.viewpager.widget.*
import com.google.android.material.tabs.*
import com.google.android.material.tabs.TabLayout.*
import com.projects.allnotificationblocker.blockthemall.Activities.Rules.*
import com.projects.allnotificationblocker.blockthemall.Activities.Rules.RulesManager.Companion.fromJson
import com.projects.allnotificationblocker.blockthemall.Fragments.Applications.ApplicationsFragment.*
import com.projects.allnotificationblocker.blockthemall.R
import com.projects.allnotificationblocker.blockthemall.Utilities.*
import com.projects.allnotificationblocker.blockthemall.Utilities.Util.loadRulesManager
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*
import com.projects.allnotificationblocker.blockthemall.domain.*
import timber.log.*

class ExceptionsActivity: AppCompatActivity(), View.OnClickListener,
    ApplicationsFragmentListener {
    override var rulesManager: RulesManager? = null
    override var exceptions: ArrayList<String?> = ArrayList<String?>()
    var profileString: String? = null
    var tabs: TabLayout? = null
    var sectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mButtonBackImage: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exceptions)
//        Util.enableEdgeToEdge16(findViewById(R.id.main))

        profileString = intent.getStringExtra(Constants.PARAM_SELECTED_PROFILE)
        if (profileString == null) {
            rulesManager = RulesManager()
        } else if (profileString!!.isEmpty()) {
            Timber.tag("AppInfo").d("profileString == null || profileString.isEmpty()")
            rulesManager = loadRulesManager()
        } else {
            Timber.tag("AppInfo").d("profileString: %s", profileString)
            val p = Profile.fromJson(profileString)
            rulesManager = fromJson(p.rules)
        }

        Timber.tag("AppInfo").d("exceptions.size: %d", exceptions.size)
        for (i in exceptions.indices) {
            Timber.tag("AppInfo").d("Exception: %s", exceptions.get(i))
        }
        rulesManager!!.logAllRules()

        exceptions.clear()
        exceptions.addAll(rulesManager!!.exceptions)

        initView()
    }

    private fun initView() {
        sectionsPagerAdapter =
            SectionsPagerAdapter(this, supportFragmentManager, Constants.MODE_EXCEPTIONS)
        tabs = findViewById<TabLayout>(R.id.tabs)
        val mPagerView = findViewById<ViewPager>(R.id.view_pager)
        mPagerView.adapter = sectionsPagerAdapter
        tabs!!.setupWithViewPager(mPagerView)

        val mSelectButton = findViewById<Button>(R.id.button_select)
        mSelectButton.setOnClickListener(this)


        // Iterate over all tabs and set the custom view
        for (i in 0..<tabs!!.tabCount) {
            val tab = tabs!!.getTabAt(i)
            tab!!.customView = sectionsPagerAdapter!!.getTabView(i)
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


        //  Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        mButtonBackImage = findViewById<ImageButton>(R.id.image_button_back)
        mButtonBackImage!!.setOnClickListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { //finish();
            onBackPressed()
        }
        return true
    }

    override fun onBackPressed() {
        //Execute your code here
        super.onBackPressed()
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onClick(v: View) {
        if (v.id == R.id.button_select) {
            val intent = Intent()
            intent.putStringArrayListExtra(Constants.PARAM_EXCEPTIONS, exceptions)
            setResult(RESULT_OK, intent)
            finish()
        }

        if (v.id == R.id.image_button_back) {
            onBackPressed()
        }
    }

    override fun selectApp(appInfo: AppInfo) {
        exceptions.add(appInfo.packageName)
    }

    override fun unselectApp(appInfo: AppInfo) {
        exceptions.remove(appInfo.packageName)
    }


    override fun refreshHome() {
    }
}


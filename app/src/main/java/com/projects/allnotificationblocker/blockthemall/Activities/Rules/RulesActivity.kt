package com.projects.allnotificationblocker.blockthemall.Activities.Rules

import android.content.*
import android.content.pm.*
import android.graphics.drawable.*
import android.os.*
import android.view.*
import android.widget.*
import androidx.appcompat.app.*
import androidx.recyclerview.widget.*
import com.bumptech.glide.*
import com.google.android.material.floatingactionbutton.*
import com.mikhaellopez.circularimageview.*
import com.projects.allnotificationblocker.blockthemall.Activities.Rules.NewRuleDialog.*
import com.projects.allnotificationblocker.blockthemall.Activities.Rules.NewRuleDialog.Companion.newInstance
import com.projects.allnotificationblocker.blockthemall.Activities.Rules.RulesAdapter.*
import com.projects.allnotificationblocker.blockthemall.Dialogs.*
import com.projects.allnotificationblocker.blockthemall.R
import com.projects.allnotificationblocker.blockthemall.Utilities.*
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*
import com.projects.allnotificationblocker.blockthemall.domain.*
import timber.log.*

class RulesActivity: AppCompatActivity(), View.OnClickListener, RulesAdapterListener,
    DialogListener {
    var rulesManager: RulesManager? = null
    override var mode: Int = 0
    var appInfo: AppInfo? = null
    private var textViewAppName: TextView? = null
    private var mRecyclerViewRules: RecyclerView? = null
    private var rulesAdapter: RulesAdapter? = null
    private var packageName: String = ""
    private var mImageViewAppicon: CircularImageView? = null
    private var dataType = -1
    private var rules: MutableList<Rule> = ArrayList<Rule>()
    private var mButtonBackImage: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rules)
        Util.enableEdgeToEdge16(findViewById(R.id.main))

        Timber.tag("AppInfo").d("RulesActivity")

        val s = intent.getStringExtra(Constants.PARAM_RULES_MANAGER)
        Timber.tag("AppInfo").d(" s : %s", s)
        if (s != null && !s.isEmpty()) {
            rulesManager = RulesManager.fromJson(s)
            rulesManager!!.logAllRules()
        } else {
            rulesManager = RulesManager()
        }

        mode = intent.getIntExtra(Constants.PARAM_MODE, -1)


        packageName = intent.getStringExtra(Constants.PARAM_PACKAGE_NAME)!!
        dataType = intent.getIntExtra(Constants.PARAM_DATA_TYPE, -1)

        initView()
        setData()

        if (rulesManager != null) {
            if (dataType == Constants.DATA_TYPE_APPLICATION || dataType == Constants.DATA_TYPE_BLOCK_ALL) {
                rules = rulesManager!!.getCustomRules(packageName)
            } else if (dataType == Constants.DATA_TYPE_CONTACT) {
                rules = rulesManager!!.getCustomRules(Util.getPhoneId(packageName))
            }
            Timber.tag("AppInfo").d("rules size: %d", rules.size)
            for (i in rules.indices) {
                rules[i]!!.logRule()
            }
        }

        setAdapter()
    }

    private fun initView() {
        textViewAppName = findViewById<TextView>(R.id.text_view_app_name)
        mRecyclerViewRules = findViewById<RecyclerView>(R.id.rules_recycler_view)
        val mFabNewrule = findViewById<FloatingActionButton>(R.id.newrule_fab)
        mFabNewrule.setOnClickListener(this)
        mImageViewAppicon = findViewById<CircularImageView>(R.id.appicon_image_view)
        setAdapter()


        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mButtonBackImage = findViewById<ImageButton>(R.id.image_button_back)
        mButtonBackImage!!.setOnClickListener(this)
    }

    private fun setData() {
        when (dataType) {
            Constants.DATA_TYPE_APPLICATION -> {
                appInfo = AppInfo.getAppInfo(applicationContext, packageName)
                if (appInfo == null) {
                    return
                }
                textViewAppName!!.text = appInfo!!.appName

                var appIcon: Drawable? = null
                try {
                    appIcon = packageManager
                        .getApplicationIcon(appInfo!!.packageName!!)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }

                if (appIcon != null) {
                    Glide.with(this).load(appIcon).into(mImageViewAppicon!!)
                } else {
                    Glide.with(this).load(R.drawable.appicon1).into(mImageViewAppicon!!)
                }

            }

            Constants.DATA_TYPE_BLOCK_ALL -> {
                textViewAppName!!.setText(R.string.block_all)
                mImageViewAppicon!!.setImageResource(R.drawable.appicon)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { //do whatever
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        //Execute your code here
        super.onBackPressed()
        val intent = Intent()
        intent.putExtra(Constants.PARAM_RULES_MANAGER, rulesManager!!.toJson())
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun setAdapter() {
        rulesAdapter = RulesAdapter(this, rules)
        mRecyclerViewRules!!.setHasFixedSize(true)
        mRecyclerViewRules!!.setItemViewCacheSize(20)
        mRecyclerViewRules!!.setDrawingCacheEnabled(true)
        mRecyclerViewRules!!.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH

        // use a linear layout manager
        val layoutManager = LinearLayoutManager(this)
        mRecyclerViewRules!!.layoutManager = layoutManager

        mRecyclerViewRules!!.adapter = rulesAdapter
    }

    override fun onClick(v: View) {
        if (v.id == R.id.newrule_fab) {
            val fm = supportFragmentManager
            val newRuleDialog = newInstance(-1, mode)
            newRuleDialog.show(fm, "fragment_edit_name")
        }

        if (v.id == R.id.image_button_back) {
            onBackPressed()
        }
    }

    override fun removeRule(rule: Rule) {
        val dialog = ConfirmDialog(
            this,
            getString(R.string.are_you_sure_remove_rule)
        )
        dialog.show()
        dialog.setCancelable(true)
        dialog.setOnDismissListener(object: DialogInterface.OnDismissListener {
            override fun onDismiss(dialogInterface: DialogInterface?) {
                if (dialog.result) {
                    rule.stopTimer(applicationContext)
                    rulesManager!!.rules.remove(rule)
                    rules.remove(rule)
                    if (mode != Constants.MODE_PROFILE) {
                        Util.saveRulesManager(rulesManager!!)
                    }
                    rulesAdapter!!.notifyDataSetChanged()
                }
            }
        })
    }

    override fun onEditRuleClicked(rule: Rule) {
        val fm = supportFragmentManager
        val newRuleDialog = newInstance(rule.pkey, mode)
        newRuleDialog.show(fm, "fragment_edit_name")
    }

    override fun updateRule(pkey: Int, schedule: Schedule) {
        val updatedRule = rulesManager!!.editCustomRule(pkey, schedule)
        updatedRule?.scheduleTimers(applicationContext)
        if (mode != Constants.MODE_PROFILE) {
            Util.saveRulesManager(rulesManager!!)
        }
        rulesAdapter!!.notifyDataSetChanged()
    }

    override fun saveNewRule(schedule: Schedule) {
        Timber.tag("AppInfo").d("saveNewRule")
        if (rulesManager == null) {
            return
        }
        val rule = if (dataType == Constants.DATA_TYPE_APPLICATION) {
            rulesManager!!.addCustomRule(
                appInfo!!.packageName,
                schedule, mode
            )
        } else {
            rulesManager!!.addCustomRule(
                packageName,
                schedule, mode
            )
        }
        rules.add(rule)
        rule.scheduleTimers(applicationContext)
        if (mode != Constants.MODE_PROFILE) {
            Util.saveRulesManager(rulesManager!!)
        }
        rulesAdapter!!.notifyDataSetChanged()

    }
}

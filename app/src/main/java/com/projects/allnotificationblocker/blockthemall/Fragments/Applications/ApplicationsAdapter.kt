package com.projects.allnotificationblocker.blockthemall.Fragments.Applications

import android.content.*
import android.content.pm.*
import android.graphics.*
import android.graphics.drawable.*
import android.view.*
import android.widget.*
import androidx.cardview.widget.*
import androidx.core.content.*
import androidx.fragment.app.*
import androidx.recyclerview.widget.*
import com.bumptech.glide.*
import com.google.android.material.switchmaterial.*
import com.makeramen.roundedimageview.*
import com.projects.allnotificationblocker.blockthemall.Activities.Rules.*
import com.projects.allnotificationblocker.blockthemall.Application.*
import com.projects.allnotificationblocker.blockthemall.Fragments.Applications.ApplicationsAdapter.*
import com.projects.allnotificationblocker.blockthemall.R
import com.projects.allnotificationblocker.blockthemall.Utilities.*
import com.projects.allnotificationblocker.blockthemall.domain.*
import java.util.*

class ApplicationsAdapter(
    private val fragment: Fragment,
  val  apps: MutableList<AppInfo>,
    var mode: Int,
): RecyclerView.Adapter<AppInfoViewHolder?>() {
    private var listener: ApplicationAdapterListener
    var originalSize: Int = 0
    var userFilter: String? = null

     var mApps = apps
        set(value) {
            field = if (userFilter.isNullOrEmpty()) {
                apps
            } else {
                apps.filter { it.appName.contains(userFilter!!, ignoreCase = true) }
                    .toMutableList()
            }
            value.sort()
        }


    init {
        if (fragment is ApplicationAdapterListener) {
            listener = fragment as ApplicationAdapterListener
        } else {
            throw RuntimeException(
                fragment.toString()
                        + " must implement ApplicationAdapterListener"
            )
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): AppInfoViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_item_app, viewGroup, false)
        return AppInfoViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return mApps.size
    }

    private fun reArrange() {
        // Sort apps to show enabled ones first
        mApps.sort()
        // Notify adapter of data change - this will rebind all views with correct states
        this.notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: AppInfoViewHolder, pos: Int) {
        val appInfo = mApps[pos]
        val genericAppInfoViewHolder = holder
        genericAppInfoViewHolder.textViewAppName!!.text = appInfo.appName


        //        genericViewHolder.imageViewAppIcon
//                .setImageDrawable(MyApplication.getAppIcon(appInfo.getPackageName()));
        var appIcon: Drawable? = null //MyApplication.getIcon(appInfo.getPackageName());
        try {
            appIcon = fragment.requireContext().packageManager.getApplicationIcon(appInfo.packageName)
            MyApplication.context
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        Glide.with(fragment).load(appIcon).into(genericAppInfoViewHolder.imageViewAppIcon!!)
        if (mode == Constants.MODE_HOMEPAGE || mode == Constants.MODE_PROFILE) {
            // Remove listener first to prevent triggering during state update
            genericAppInfoViewHolder.switchEnableDisableRule!!.setOnCheckedChangeListener(null)
            
            // Update the UI state based on current rules
            updateEnabledRulesCount(genericAppInfoViewHolder, appInfo)

            // Set listener after state is updated
            genericAppInfoViewHolder.switchEnableDisableRule!!.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { compoundButton: CompoundButton?, b: Boolean ->
                    // Prevent recursive calls - verify the state actually changed
                    val currentState = listener.rulesManager?.hasPermenantEnabledRule(appInfo.packageName) == true ||
                            listener.rulesManager?.hasCustomEnabledValidRules(appInfo.packageName) == true
                    if (b == currentState) {
                        // State already matches, ignore this trigger
                        return@OnCheckedChangeListener
                    }
                    
                    if (b) {
                        listener.enableRules(appInfo)
                        appInfo.isEnabled = true
                    } else {
                        listener.disableRules(appInfo)
                        appInfo.isEnabled = false
                    }
                    // Update UI for this specific item only
                    updateEnabledRulesCount(genericAppInfoViewHolder, appInfo)
                    // Refresh all items to ensure consistency
                    notifyDataSetChanged()
                })
        } else {
            genericAppInfoViewHolder.imageViewRuleIcon!!.setVisibility(View.GONE)
            genericAppInfoViewHolder.textViewRules!!.visibility = View.GONE
            genericAppInfoViewHolder.switchEnableDisableRule!!.setOnCheckedChangeListener(null)
            val isException = listener.exceptions.contains(appInfo.packageName)
            genericAppInfoViewHolder.switchEnableDisableRule!!.isChecked = isException
            appInfo.isEnabled = isException
            genericAppInfoViewHolder.switchEnableDisableRule!!.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { compoundButton: CompoundButton?, b: Boolean ->
                    // Prevent recursive calls
                    val currentState = listener.exceptions.contains(appInfo.packageName)
                    if (b == currentState) {
                        return@OnCheckedChangeListener
                    }
                    
                    if (b) {
                        listener.selectApp(appInfo)
                        appInfo.isEnabled = true
                    } else {
                        listener.unselectApp(appInfo)
                        appInfo.isEnabled = false
                    }
                    notifyDataSetChanged()
                })
        }

        genericAppInfoViewHolder.buttonCustomRules!!.setOnClickListener(object:
            View.OnClickListener {
            override fun onClick(view: View?) {
                listener.customRules(appInfo)
            }
        })
    }

    private fun updateEnabledRulesCount(
        genericAppInfoViewHolder: AppInfoViewHolder,
        appInfo: AppInfo,
    ) {
        val rulesManager = listener.rulesManager ?: return
        val isBlockAllEnabled = rulesManager.isBlockAllEnabled ||
                rulesManager.hasCustomEnabledValidRules(Constants.RULE_BLOCK_ALL) == true
        
        if (isBlockAllEnabled) {
            // Block All is enabled - hide switch and show blocked state
            genericAppInfoViewHolder.switchEnableDisableRule!!.visibility = View.GONE
            genericAppInfoViewHolder.textViewRules!!.setText(R.string.always_block)
            genericAppInfoViewHolder.imageViewRuleIcon!!.setImageResource(R.drawable.ic_block)
            genericAppInfoViewHolder.imageViewRuleIcon!!.setColorFilter(
                ContextCompat.getColor(
                    MyApplication.context,
                    R.color.red
                ), PorterDuff.Mode.SRC_IN
            )
            genericAppInfoViewHolder.imageViewRuleIcon!!.visibility = View.VISIBLE
            genericAppInfoViewHolder.textViewRules!!.visibility = View.VISIBLE
        } else {
            // Block All is not enabled - show switch and individual app state
            genericAppInfoViewHolder.switchEnableDisableRule!!.visibility = View.VISIBLE
            genericAppInfoViewHolder.imageViewRuleIcon!!.visibility = View.VISIBLE
            genericAppInfoViewHolder.textViewRules!!.visibility = View.VISIBLE

            // Check individual app's rule state
            val hasCustomRules = rulesManager.hasCustomEnabledValidRules(appInfo.packageName) == true
            val hasPermanentRule = rulesManager.hasPermenantEnabledRule(appInfo.packageName)
            
            if (hasCustomRules) {
                // App has custom schedule rules
                genericAppInfoViewHolder.textViewRules!!.setText(R.string.custom_schedule)
                genericAppInfoViewHolder.switchEnableDisableRule!!.isChecked = true
                genericAppInfoViewHolder.imageViewRuleIcon!!.setImageResource(R.drawable.ic_time)
                genericAppInfoViewHolder.imageViewRuleIcon!!.setColorFilter(
                    ContextCompat.getColor(
                        MyApplication.context,
                        R.color.colorAccent
                    ), PorterDuff.Mode.SRC_IN
                )
                appInfo.isEnabled = true
            } else if (hasPermanentRule) {
                // App has permanent block rule
                    genericAppInfoViewHolder.textViewRules!!.setText(R.string.always_block)
                    genericAppInfoViewHolder.switchEnableDisableRule!!.isChecked = true
                    genericAppInfoViewHolder.imageViewRuleIcon!!.setImageResource(R.drawable.ic_block)
                    genericAppInfoViewHolder.imageViewRuleIcon!!.setColorFilter(
                        ContextCompat.getColor(
                            MyApplication.context,
                            R.color.red
                        ), PorterDuff.Mode.SRC_IN
                    )
                appInfo.isEnabled = true
                } else {
                // App has no rules - not blocked
                    genericAppInfoViewHolder.switchEnableDisableRule!!.isChecked = false
                    genericAppInfoViewHolder.textViewRules!!.setText(R.string.no_schedules_applied)
                    genericAppInfoViewHolder.imageViewRuleIcon!!.setImageResource(R.drawable.ic_check)
                    genericAppInfoViewHolder.imageViewRuleIcon!!.setColorFilter(
                        ContextCompat.getColor(
                            MyApplication.context,
                            R.color.green
                        ), PorterDuff.Mode.SRC_IN
                    )
                appInfo.isEnabled = false
            }
        }
    }




    interface ApplicationAdapterListener {
        fun enableRules(appInfo: AppInfo?)

        fun disableRules(appInfo: AppInfo?)

        val list: MutableList<AppInfo>?

        fun selectApp(appInfo: AppInfo)

        fun unselectApp(appInfo: AppInfo)

        val exceptions: ArrayList<String>

        val rulesManager: RulesManager?

        fun customRules(appInfo: AppInfo)

        fun getMode(): Int
    }

    class AppInfoViewHolder internal constructor(v: View): RecyclerView.ViewHolder(v) {
        var isResized: Boolean = false
        var textViewRules: TextView? = null
        var imageViewAppIcon: RoundedImageView? = null
        var textViewAppName: TextView? = null
        var switchEnableDisableRule: SwitchMaterial? = null
        var imageViewRuleIcon: ImageView? = null
        var cardView: CardView? = null
        var buttonCustomRules: Button? = null

        init {
            initView(v)
        }

        fun initView(itemView: View) {
            imageViewAppIcon = itemView.findViewById<RoundedImageView>(R.id.image_view_app_icon)
            textViewAppName = itemView.findViewById<TextView>(R.id.text_view_app_name)
            textViewRules = itemView.findViewById<TextView>(R.id.text_view_app_rules)
            switchEnableDisableRule = itemView.findViewById<SwitchMaterial>(R.id.switch_app)
            imageViewRuleIcon = itemView.findViewById<ImageView>(R.id.image_view_rule_icon)
            cardView = itemView.findViewById<CardView>(R.id.card_view)
            buttonCustomRules = itemView.findViewById<Button>(R.id.button_custom_rules)
        }
    }
}
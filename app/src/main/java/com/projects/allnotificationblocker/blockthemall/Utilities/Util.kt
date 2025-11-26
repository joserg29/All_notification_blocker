package com.projects.allnotificationblocker.blockthemall.Utilities

import android.animation.*
import android.animation.ValueAnimator.*
import android.content.*
import android.graphics.*
import android.util.*
import android.view.*
import android.view.animation.*
import android.widget.*
import androidx.core.content.*
import androidx.core.view.*
import com.pixplicity.easyprefs.library.*
import com.projects.allnotificationblocker.blockthemall.Activities.Rules.*
import com.projects.allnotificationblocker.blockthemall.R
import timber.log.*


object Util {
    private const val NOTIFICATION_ID = 650300
    var timeMap: MutableMap<String?, Long?> = HashMap<String?, Long?>()

    fun resizeAnimateViewWidth(view: View, finalWidth: Int, speed: Int) {
        // set the values we want to animate between and how long it takes
        // to run
        val slideAnimator = ofInt(view.width, finalWidth)
            .setDuration(speed.toLong())


        // we want to manually handle how each tick is handled so add a
        // listener
        slideAnimator.addUpdateListener(AnimatorUpdateListener { animation: ValueAnimator? ->
            // get the value the interpolator is at
            val value = animation!!.getAnimatedValue() as Int
            // I'm going to set the layout's height 1:1 to the tick
            view.layoutParams.width = value
            // force all layouts to see which ones are affected by
            // this layouts height change
            view.requestLayout()
        })

        // create a new animationset
        val set = AnimatorSet()
        // since this is the only animation we are going to run we just use
        // play
        set.play(slideAnimator)
        // this is how you set the parabola which controls acceleration
        set.interpolator = AccelerateDecelerateInterpolator()

        // start the animation
        set.start()
    }

    fun getPhoneId(p: String): String {
        var phone = p
        phone = phone.replace("(", "")
        phone = phone.replace(")", "")
        phone = phone.replace("-", "")
        phone = phone.replace(" ", "")
        if (phone.length > 10) {
            phone = phone.substring(phone.length - 10)
        }
        return phone
    }

    fun updateBlockStatus(
        manager: RulesManager,
        context: Context,
        notificationsStatusView: TextView,
        callsStatusView: TextView,
        appRuleIconView: ImageView,
        contactRuleIconView: ImageView,
        displayMode: Int,
    ) {
        val isAllBlocked = manager.isBlockAllEnabled
        if (isAllBlocked) {
            Timber.tag("AppInfo").d("manager.isBlockAllEnabled() = true")
            notificationsStatusView.text = context.getString(R.string.all_notifications_blocked)
            callsStatusView.text = context.getString(R.string.all_calls_blocked)

            appRuleIconView.setImageResource(R.drawable.ic_block)
            appRuleIconView.setColorFilter(
                ContextCompat.getColor(
                    context,
                    R.color.red
                ), PorterDuff.Mode.SRC_IN
            )

            contactRuleIconView.setImageResource(R.drawable.ic_block)
            contactRuleIconView.setColorFilter(
                ContextCompat.getColor(
                    context,
                    R.color.red
                ), PorterDuff.Mode.SRC_IN
            )

            if (displayMode == Constants.MODE_HOMEPAGE) {
                NotificationsUtil.upsertPermanentNotification(
                    context,
                    NOTIFICATION_ID,
                    context.getString(R.string.all_notifications_calls_blocked),
                    "",
                    R.drawable.appicon
                )
            }
            return
        }

        val isScheduledBlocking = manager.hasCustomEnabledValidRules(Constants.RULE_BLOCK_ALL)
        if (isScheduledBlocking == true) {

            notificationsStatusView.text =
                context.getString(R.string.all_notifications_blocked_schedule)
            callsStatusView.text = context.getString(R.string.all_calls_blocked_schedule)

            appRuleIconView.setImageResource(R.drawable.ic_time)
            appRuleIconView.setColorFilter(
                ContextCompat.getColor(
                    context,
                    R.color.colorAccent
                ), PorterDuff.Mode.SRC_IN
            )

            contactRuleIconView.setImageResource(R.drawable.ic_time)
            contactRuleIconView.setColorFilter(
                ContextCompat.getColor(
                    context,
                    R.color.colorAccent
                ), PorterDuff.Mode.SRC_IN
            )

            if (displayMode == Constants.MODE_HOMEPAGE) {
                NotificationsUtil.upsertPermanentNotification(
                    context,
                    NOTIFICATION_ID,
                    context.getString(R.string.all_notifications_calls_blocked_schedule),
                    "",
                    R.drawable.appicon
                )
            }
            return
        }

        var notificationStatus = ""
        var callStatus = ""

        Timber.tag("AppInfo").d(
            "getRulesManager()" +
                    ".hasPermenantOrCustomEnabledValidRulesForApps() = " +
                    manager.hasPermenantOrCustomEnabledValidRulesForApps()
        )

        if (manager.hasPermenantOrCustomEnabledValidRulesForApps() == true) {
            notificationStatus = context.getString(R.string.some_notifications_blocked)
            notificationsStatusView.text = notificationStatus

            appRuleIconView.setImageResource(R.drawable.ic_block)
            appRuleIconView.setColorFilter(
                ContextCompat.getColor(
                    context,
                    R.color.red
                ), PorterDuff.Mode.SRC_IN
            )
        } else {
            notificationStatus = ""
            notificationsStatusView.text = context.getString(R.string.no_notifications_blocked)
            appRuleIconView.setImageResource(R.drawable.ic_check)
            appRuleIconView.setColorFilter(
                ContextCompat.getColor(
                    context,
                    R.color.green
                ), PorterDuff.Mode.SRC_IN
            )
        }

        Timber.tag("AppInfo").d(
            "manager.hasPermenantOrCustomEnabledValidRulesForCalls() = %s",
            manager.hasPermenantOrCustomEnabledValidRulesForCalls()
        )
        if (manager.hasPermenantOrCustomEnabledValidRulesForCalls() == true) {
            callStatus = "Some Calls\n Blocked"
            callsStatusView.text = callStatus

            contactRuleIconView.setImageResource(R.drawable.ic_block)
            contactRuleIconView.setColorFilter(
                ContextCompat.getColor(
                    context,
                    R.color.red
                ), PorterDuff.Mode.SRC_IN
            )
        } else {
            callStatus = ""
            callsStatusView.text = context.getString(R.string.no_notifications_blocked)
            contactRuleIconView.setImageResource(R.drawable.ic_check)
            contactRuleIconView.setColorFilter(
                ContextCompat.getColor(
                    context,
                    R.color.green
                ), PorterDuff.Mode.SRC_IN
            )
        }

        if (displayMode == Constants.MODE_HOMEPAGE) {
            Timber.tag("AppInfo").d("status1 = %s", notificationStatus)
            Timber.tag("AppInfo").d("status2 = %s", callStatus)
            if (!notificationStatus.isEmpty() && !callStatus.isEmpty()) {
                NotificationsUtil.upsertPermanentNotification(
                    context,
                    NOTIFICATION_ID,
                    "Some Notifications/Calls are Blocked",
                    "",
                    R.drawable.appicon
                )
            }

            if (!notificationStatus.isEmpty() && callStatus.isEmpty()) {
                NotificationsUtil.upsertPermanentNotification(
                    context,
                    NOTIFICATION_ID,
                    "Some Notifications are Blocked",
                    "",
                    R.drawable.appicon
                )
            }

            if (notificationStatus.isEmpty() && !callStatus.isEmpty()) {
                NotificationsUtil.upsertPermanentNotification(
                    context,
                    NOTIFICATION_ID,
                    "Some Calls are Blocked",
                    "",
                    R.drawable.appicon
                )
            }


            if (notificationStatus.isEmpty() && callStatus.isEmpty()) {
                NotificationsUtil.upsertPermanentNotification(
                    context,
                    NOTIFICATION_ID,
                    "No Notifications/Calls are Blocked",
                    "",
                    R.drawable.appicon
                )
            }
        }
    }

    fun extractText(extras: String, s1: String, s2: String): String {
        val start = extras.indexOf(s1)
        val start1 = start + s1.length
        val end1 = extras.length
        if (start > -1) {
            var title = extras.substring(start1, end1)
            val end2 = title.indexOf(s2)
            if (end2 > -1) {
                title = title.substring(0, title.indexOf(s2))
            }
            return title
        }

        return ""
    }
    fun enableEdgeToEdge16(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { view, insets ->
            val innerPadding = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(
                innerPadding.left,
                innerPadding.top,
                innerPadding.right,
                innerPadding.bottom
            )
            insets
        }
    }

    fun loadRulesManager(): RulesManager? {
        val rulesManagerString = Prefs.getString(Constants.PARAM_RULES_MANAGER, "")
        if (rulesManagerString != null && !rulesManagerString.isEmpty()) {
            return RulesManager.fromJson(rulesManagerString)?.also {
        Log.i("AppProfile", "Loading RulesManager from Prefs rulesNum:${it.rules.size}")

            }
        }
        return RulesManager()
    }

    fun saveRulesManager(rulesManager: RulesManager) {
        Log.i("AppProfile", "Saving RulesManager to Prefs rulesNum:${rulesManager.rules.size}")
        // Save synchronously to avoid race conditions when service needs to read immediately
        val rulesManagerString = rulesManager.toJson()
        Prefs.putString(Constants.PARAM_RULES_MANAGER, rulesManagerString)
        Timber.tag("Util").d("RulesManager saved synchronously with %d rules", rulesManager.rules.size)
    }

    fun finishProfile(name: String?, depth: Int) {
        val finish = System.currentTimeMillis()
        val diff = finish - timeMap[name]!!
        val s = StringBuilder()
        for (i in 0..<depth) {
            s.append(" ")
        }
        Log.i("AppProfile", ">>>" + s + "[" + name + "]: " + diff + " ms")
    }

    fun startProfile(name: String?) {
        val start = System.currentTimeMillis()
        timeMap.put(name, start)
    }
}

package com.projects.allnotificationblocker.blockthemall.data.db.entities

import android.content.*
import androidx.room.*
import androidx.room.Entity
import com.projects.allnotificationblocker.blockthemall.R
import com.projects.allnotificationblocker.blockthemall.Utilities.*
import com.projects.allnotificationblocker.blockthemall.Utilities.scheduler.RuleScheduler
import com.projects.allnotificationblocker.blockthemall.domain.*
import java.util.*

@Entity(tableName = "rules_table")
data class Rule(
    var status: Int = -1,
    @PrimaryKey(autoGenerate = true) var pkey: Int = 0,
    var packageName: String,
    var ruleType: Int = 0,
    var isEnabled: Boolean,
    var everyday: Boolean = false,
    var schedule: Schedule,
    var profileId: Int? = null,
    var alarmId: String = UUID.randomUUID().toString(),
) {

    init {
        if (alarmId.isEmpty()) {
            alarmId = UUID.randomUUID().toString()
        }
    }

    @Ignore fun logRule() {/*    Timber.tag("AppInfo").d(
                "### RULE: [%s] from: %s, to: %s, enabled: %s, inActive: %s, Active: %s, Expired: %s",
                packageName,
                from,
                to,
                this.isEnabled, isInactive(Constants.MODE_HOMEPAGE),
                isActive(Constants.MODE_HOMEPAGE),
                isExpired(Constants.MODE_HOMEPAGE)
            )*/
    }

    fun  getRuleTypeString(context: Context): String {
            return when (ruleType) {
                Constants.RULE_TYPE_CUSTOM -> context.getString(R.string.custom_schedule)
                Constants.RULE_TYPE_PERMENANT -> context.getString(R.string.permanent_schedule)
                else -> ""

            }
        }

    val isPermenant: Boolean
        get() = ruleType == Constants.RULE_TYPE_PERMENANT

    val isCustom: Boolean
        get() = ruleType == Constants.RULE_TYPE_CUSTOM

    fun isActive(): Boolean = schedule.isActive(Date())


    fun isInactive(): Boolean = !isActive()

    fun isExpired(): Boolean = schedule.isOutDated(Date())


    fun disableRule() {
        this.isEnabled = false
    }

    val isApp: Boolean
        get() = packageName.startsWith("com.")

    val isCall: Boolean
        get() = !packageName.startsWith("com.")


    fun scheduleTimers(context: Context) {
        RuleScheduler.schedule(context, this)
    }

    fun stopTimer(context: Context) {
        RuleScheduler.cancel(context, this)
    }

    companion object {
        fun newPermenantRule(packageName: String, profileId: Int? = null): Rule {
            val rule = Rule(
                packageName = packageName,
                ruleType = Constants.RULE_TYPE_PERMENANT,
                isEnabled = true,
                everyday = false,
                schedule = Schedule(),
                profileId = profileId
            )
            return rule
        }
    }

    /*
     fun startTimer(context: Context) {

         //used for register alarm manager
         val pendingIntent: PendingIntent

         val intent = Intent("com.techblogon.alarmexample")
         intent.putExtra("rule_type", this.ruleType)
         intent.putExtra("package_name", packageName)
         //intent.putExtra("data_type", dataType);
         intent.putExtra("schedule", ScheduleConverters().toJson(schedule))

         pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
         //used to store running alarmmanager instance
         val alarmManager: AlarmManager =
             (context.getSystemService(Context.ALARM_SERVICE)) as AlarmManager

         val to = DateTimeUtil.DateStringToLong(this.to)

         checkNotNull(alarmManager)
         alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, to, pendingIntent)
     }
     */

    /*fun isExpired(mode: Int): Boolean {

        val currentTime = DateTimeUtil.currentDateTime
        return if (mode == Constants.MODE_HOMEPAGE) {
            DateTimeUtil.isDate1AfterDate2(currentTime, this.to)
        } else {
            DateTimeUtil.isTime1AfterTime2(currentTime, this.to)
        }
    }
*/

    /*
        fun isInactive(mode: Int): Boolean {
            val currentTime = DateTimeUtil.currentDateTime

            //   tag("AppInfo").d("isInactive - CurrentTime: %s, From: %s, To: %s", currentTime, getFrom(), getTo());
            if (mode == Constants.MODE_HOMEPAGE) {
                return DateTimeUtil.isDate1AfterDate2(this.from, currentTime)
            } else {
                return DateTimeUtil.isTime1AfterTime2(this.from, currentTime)
            }
        }
    */

    /*
    fun isActive(mode: Int): Boolean {
           val currentTime = DateTimeUtil.currentDateTime

           // tag("AppInfo").d("isInactive - CurrentTime: %s, From: %s, To: %s", currentTime, getFrom(), getTo());
           val isBetweenDateTimes: Boolean
           if (mode == Constants.MODE_HOMEPAGE) {
               isBetweenDateTimes = DateTimeUtil.isDateBetweenDates(currentTime, this.from, this.to)
           } else {
               isBetweenDateTimes = DateTimeUtil.isTimeBetweenTimes(currentTime, this.from, this.to)
           }


           return isBetweenDateTimes
       }
   */
}

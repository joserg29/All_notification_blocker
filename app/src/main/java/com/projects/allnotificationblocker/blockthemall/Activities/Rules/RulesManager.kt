package com.projects.allnotificationblocker.blockthemall.Activities.Rules

import com.google.gson.*
import com.pixplicity.easyprefs.library.*
import com.projects.allnotificationblocker.blockthemall.Application.*
import com.projects.allnotificationblocker.blockthemall.BuildConfig
import com.projects.allnotificationblocker.blockthemall.Utilities.*
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*
import com.projects.allnotificationblocker.blockthemall.domain.*
import timber.log.*
import java.util.*

class RulesManager {
    var exceptions: ArrayList<String> = ArrayList<String>()
    var rules: MutableList<Rule> = mutableListOf()

    private fun saveRules() {
        val jsonString = toJson(rules)
        Prefs.putString("rules", jsonString)
    }

    private fun disablePermenantRule(packageName: String?) {
        for (i in rules.indices) {
            if (rules[i].packageName == packageName && rules[i].isPermenant) {
                rules[i].disableRule()
                rules[i].stopTimer(MyApplication.context)
                saveRules()
                return
            }
        }
        saveRules()
    }

    fun editCustomRule(
        pkey: Int,
        schedule: Schedule,
    ): Rule? {
        val updated = rules.find { it.pkey == pkey } ?: return null
        updated.schedule = schedule
        MyApplication.rulesViewModel.update(updated)
        return updated
    }

    fun addCustomRule(
        packageName: String,
        schedule: Schedule,
        mode: Int,
    ): Rule {
        Timber.tag(TAG).d("addCustomRule: %s", packageName)
        val rule = Rule(
            schedule = schedule,
            ruleType = Constants.RULE_TYPE_CUSTOM,
            packageName = packageName,
            isEnabled = true,
        )
        if (mode == Constants.MODE_PROFILE) {
            rule.everyday = true
        }
        disablePermenantRule(packageName)
        rules.add(rule)
        return rule
    }

    fun enableRule(packageName: String?, viewModel: RulesViewModel?) {
        for (i in rules.indices) {
            if (rules[i].packageName!! == packageName) {
                rules[i].isEnabled = true
                viewModel?.update(rules[i])
            }
        }
        saveRules()
    }

    fun addPermanentRule(packageName: String) {
        val rule = Rule.newPermenantRule(packageName)
        rules.add(rule)
        saveRules()
    }

    fun disableRule(packageName: String?) {
        for (i in rules.indices) {
            if (rules[i].packageName == packageName) {
                rules[i].isEnabled = false
                rules[i].stopTimer(MyApplication.context)
            }
        }

        saveRules()
    }

    fun disableRule(packageName: String?, schedule: Schedule) {
        for (i in rules.indices) {
            if (rules[i].packageName == packageName &&
                rules[i].schedule == schedule
            ) {
                rules[i].isEnabled = false
                rules[i].stopTimer(MyApplication.context)
                saveRules()
                return
            }
        }

        saveRules()
    }

    fun hasPermenantRule(packageName: String?): Boolean {
        for (i in rules.indices) {
            if (rules[i].packageName == packageName &&
                rules[i].isPermenant
            ) {
                return true
            }
        }
        return false
    }

    fun logAllRules() {
        Timber.tag(TAG).d("########################################################")
        Timber.tag(TAG).d("##################### ALL SYSTEM RULES #####################")
        Timber.tag(TAG).d("Number of Rules: %s", rules.size)
        /*
        for (i in rules.indices) {
            var enabled = "ENABLED"
            if (!rules[i].isEnabled) enabled = "DISABLED"

            var s = "# [ " + enabled + "][" + rules[i].packageName + "]: "
            s += "Type = " + rules[i].ruleTypeString + ", "
            s += ", Enabled = " + rules[i].isEnabled + ", "
            s += "from = " + rules[i].from + ", "
            s += "to = " + rules[i].to + ", " + "Everyday: " + rules[i]
                .everyday

            Timber.tag(TAG).d(s)
        }
        */
        Timber.tag(TAG).d("##################### Exceptions #####################")
        Timber.tag(TAG).d("Number of Exceptions: %d", exceptions.size)
        for (i in exceptions.indices) {
            val s = "# " + exceptions[i]
            Timber.tag(TAG).d("%s", s)
        }
        Timber.tag(TAG).d("########################################################")
    }

    fun logAllRules(tag: String) {
        Timber.tag(tag).d("########################################################")

        Timber.tag(tag).d("##################### ALL SYSTEM RULES #####################")
        Timber.tag(tag).d("Number of Rules: %s", rules.size)
        /*
                for (i in rules.indices) {
                    var s = "# [" + rules[i].packageName + "]: "
                    s += "Type = " + rules[i].ruleTypeString
                    s += ", Enabled = " + rules[i].isEnabled + ", "
                    s += "from = " + rules[i].from + ", "
                    s += "to = " + rules[i].to + ", " + "Everyday: " + rules[i]
                        .everyday

                    Timber.tag(tag).d(s)
                }
        */
        Timber.tag(tag).d("Number of Exceptions: %d", exceptions.size)
        for (i in exceptions.indices) {
            val s = "# " + exceptions[i]
            Timber.tag(tag).d("%s", s)
        }
    }

    fun getEnabledRules(packageName: String?): MutableList<Rule> {
        val packageRules: MutableList<Rule> = ArrayList<Rule>()

        for (i in rules.indices) {
            if (rules[i].packageName == packageName
                && rules[i].isEnabled
            ) {
                if (rules[i].isPermenant) {
                    packageRules.add(rules[i])
                } else if (rules[i].isCustom && !rules[i].isExpired()) {
                    packageRules.add(rules[i])
                }
            }
        }

        return packageRules
    }

    private fun toJson(rules: MutableList<Rule>?): String? {
        val gson = Gson()
        return gson.toJson(rules)
    }

    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    fun getEnabledRulesCount(packageName: String?): Int {
        var count = 0
        for (i in rules.indices) {
            if (rules[i].packageName == packageName &&
                rules[i].isEnabled
            ) {
                if (rules[i].isPermenant) {
                    count += 1
                } else if (rules[i].isCustom && !rules[i].isExpired()) {
                    count += 1
                }
            }
        }

        return count
    }

    val isBlockAllEnabled: Boolean
        get() {
            var b = false
            val generalRules =
                getEnabledRules(Constants.RULE_BLOCK_ALL)
            for (i in generalRules.indices) {
                val rule =
                    generalRules[i]

                if (rule.isCustom && rule.isEnabled && !rule.isExpired()) {
                    return false
                }

                if (rule.isPermenant && rule.isEnabled) {
                    b = true
                }
            }

            return b
        }

    fun hasPermenantEnabledRule(packageName: String?): Boolean {
        for (i in rules.indices) {
            if (rules[i].packageName == packageName &&
                rules[i].isPermenant && rules[i].isEnabled
            ) {
                return true
            }
        }

        return false
    }

    fun getCustomRules(packageName: String?): MutableList<Rule> {
        val packageRules: MutableList<Rule> = ArrayList<Rule>()
        for (i in rules.indices) {
            if (rules[i].packageName == packageName &&
                rules[i].isCustom
            ) {
                packageRules.add(rules[i])
            }
        }


        return packageRules
    }

    fun getCustomEnabledValidRules(packageName: String?): MutableList<Rule>? {

        val packageRules: MutableList<Rule> = ArrayList<Rule>()
        for (i in rules.indices) {
            if (rules[i].packageName == packageName &&
                rules[i].isCustom && rules[i].isEnabled && !rules[i]
                    .isExpired()
            ) {
                packageRules.add(rules[i])
            }
        }


        return packageRules
    }

    fun hasCustomEnabledValidRules(packageName: String?): Boolean? {
        for (i in rules.indices) {
            if (rules[i].packageName == packageName &&
                rules[i].isCustom && rules[i].isEnabled && rules[i].isActive()
            ) {
                return true
            }
        }


        return false
    }

    fun hasPermenantOrCustomEnabledValidRulesForApps(): Boolean? {
        for (i in rules.indices) {
            if (rules[i].isApp &&
                rules[i].isEnabled && rules[i].isPermenant
            ) {
                return true
            }


            if (rules[i].isApp &&
                rules[i].isEnabled && rules[i].isCustom && !rules[i]
                    .isExpired()
            ) {
                return true
            }
        }


        return false
    }

    fun hasPermenantOrCustomEnabledValidRulesForCalls(): Boolean? {
        for (i in rules.indices) {
            if (rules[i].isCall && rules[i].isEnabled && rules[i].isPermenant) {
                return true
            }
            if (rules[i].isCall && rules[i].isEnabled && rules[i].isCustom && !rules[i].isExpired()
            ) {
                return true
            }
        }

        return false
    }

    fun isAllowed(packageName: String?): Boolean {
        var isAllowed: Boolean
        if (packageName == null) {
            return true
        }
        if (BuildConfig.DEBUG) {
            logAllRules()
        }
        isAllowed = isPackageAllowed(Constants.RULE_BLOCK_ALL)
        if (!isAllowed) {
            isAllowed = exceptions.contains(packageName)
        }
        if (isAllowed) {
            isAllowed = isPackageAllowed(packageName)
        }

        return isAllowed
    }

    private fun isPackageAllowed(packageName: String?): Boolean {
        var isAllowed = true

        // tag(TAG).d("packageName:" + packageName);
        //logPackageRules(packageName);
        val isPermenant = hasPermenantEnabledRule(packageName)
        if (!isPermenant) {
            val blockAllCustomRules = getCustomEnabledValidRules(packageName)
            if (!blockAllCustomRules!!.isEmpty()) {
                val isApplicableRules =
                    isThereApplicableRules(blockAllCustomRules)
                if (isApplicableRules) {
                    isAllowed = false
                }
            }
        } else {
            isAllowed = false
        }
        return isAllowed
    }

    private fun isThereApplicableRules(
        customRules: MutableList<Rule>,
    ): Boolean {
        if (!customRules.isEmpty()) {
            for (i in customRules.indices) {
                val rule = customRules[i]
                if (rule.isActive()) {
                    return true
                }
            }
        }

        return false
    }


    fun checkCustomRules(): Boolean {
        var b = false
        for (i in rules.indices) {
            val rule = rules[i]
            if (rule.isCustom) {
                if (rule.isActive()) {
                    if (rule.status != Constants.RULES_STATUS_ACTIVE) {
                        rule.isEnabled = true
                        rule.status = Constants.RULES_STATUS_ACTIVE
                        b = true
                    }
                } else {
                    if (rule.status != Constants.RULES_STATUS_INACTIVE) {
                        rule.isEnabled = false
                        rule.status = Constants.RULES_STATUS_INACTIVE
                        b = true
                    }
                }

                if (rule.isExpired()) {
                    if (rule.status != Constants.RULES_STATUS_EXPIRED) {
                        rule.isEnabled = false
                        rule.status = Constants.RULES_STATUS_EXPIRED
                        b = true
                    }
                }
            }
        }
        return b
    }

    companion object {
        const val TAG = "RulesManager"
        fun fromJson(json: String?): RulesManager? {
            val gson = Gson()
            return gson.fromJson<RulesManager?>(json, RulesManager::class.java)
        }
    }
}

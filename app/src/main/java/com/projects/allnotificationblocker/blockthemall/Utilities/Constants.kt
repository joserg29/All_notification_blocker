package com.projects.allnotificationblocker.blockthemall.Utilities

object Constants {

    const val PARAM_PACKAGE_NAME: String = "package_name"
    const val PARAM_SELECTED_PROFILE_NAME: String = "selected_profile_name"
    const val PARAM_DATA_TYPE: String = "data_type"


    const val RULE_TYPE_PERMENANT: Int = 0
    const val RULE_TYPE_CUSTOM: Int = 1


    const val DATA_TYPE_APPLICATION: Int = 0
    const val DATA_TYPE_CONTACT: Int = 1
    const val DATA_TYPE_BLOCK_ALL: Int = 2
    const val AD_INTERVAL: Long = 1000 * 60L

    const val RULE_BLOCK_ALL: String = "block_all"
    const val UNBLOCK_ALL: Int = 1
    const val NO_BLOCK_UNBLOCK: Int = 2
    const val BLOCK_ALL: Int = 0


    const val WHATSAPP: Int = 0
    const val MESSENGER: Int = 1

    const val REQ_CODE_EXCEPTIONS: Int = 1000
    const val REQ_CODE_SELECT_PROFILE: Int = 1001
    const val REQ_CODE_NEW_RULE: Int = 1002


    const val MODE_HOMEPAGE: Int = 0
    const val MODE_EXCEPTIONS: Int = 1
    const val MODE_PROFILE: Int = 2

    const val PARAM_RULES_MANAGER: String = "rules_manager"
    const val PARAM_SELECTED_PROFILE: String = "selected_profile"

    const val PARAM_EXCEPTIONS: String = "exceptions"
    const val PARAM_MODE: String = "mode"

    const val RULES_STATUS_INACTIVE: Int = 0
    const val RULES_STATUS_ACTIVE: Int = 1
    const val RULES_STATUS_EXPIRED: Int = 2

    const val CHANNEL_ID = "blockthemall1"
    const val PARAM_NOTIFICATIONS_MANAGER: String = "notifications_manager"
    val socialMediaApps = mutableListOf(
        "com.facebook.katana",  // Facebook
        "com.facebook.lite",  // Facebook Lite
        "com.facebook.orca",  // Messenger
        "com.instagram.android",  // Instagram
        "com.twitter.android",  // Twitter
        "com.google.android.youtube",  // YouTube
        "com.linkedin.android",  // LinkedIn
        "com.snapchat.android",  // Snapchat
        "com.zhiliaoapp.musically",  // TikTok
        "com.whatsapp" // WhatsApp
    )
}

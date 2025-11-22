package com.projects.allnotificationblocker.blockthemall.Utilities

import timber.log.*

object LogUtil {
    inline fun <T> logDuration(tag: String = "timer", block: () -> T): T {
        val start = System.currentTimeMillis()
        val result = block()
        val duration = System.currentTimeMillis() - start
        Timber.tag(tag).d("executed in $duration ms")
        return result
    }
}
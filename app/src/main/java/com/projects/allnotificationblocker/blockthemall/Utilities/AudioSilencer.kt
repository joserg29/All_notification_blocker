package com.projects.allnotificationblocker.blockthemall.Utilities

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.projects.allnotificationblocker.blockthemall.Application.MyApplication
import timber.log.Timber

/**
 * Centralized controller that aggressively keeps notification-related streams muted while
 * rules require silence. It can also apply short pulses for individual blocked notifications.
 */
object AudioSilencer {
    private val handler = Handler(Looper.getMainLooper())
    private val streams = intArrayOf(
        AudioManager.STREAM_NOTIFICATION,
        AudioManager.STREAM_RING,
        AudioManager.STREAM_ALARM,
        AudioManager.STREAM_SYSTEM,
        AudioManager.STREAM_MUSIC
    )
    private const val ENFORCE_INTERVAL_MS = 30_000L
    private const val PULSE_RELEASE_DELAY_MS = 4_000L

    private var keepMuted = false
    private var hasAppliedMute = false

    private val enforceRunnable = object : Runnable {
        override fun run() {
            if (keepMuted) {
                muteStreams()
                handler.postDelayed(this, ENFORCE_INTERVAL_MS)
            }
        }
    }
    private val pulseReleaseRunnable = Runnable {
        if (!keepMuted) {
            unmuteStreams()
        }
    }

    fun enforcePersistentMute() {
        keepMuted = true
        muteStreams()
        handler.removeCallbacks(enforceRunnable)
        handler.postDelayed(enforceRunnable, ENFORCE_INTERVAL_MS)
    }

    fun releasePersistentMute() {
        keepMuted = false
        handler.removeCallbacks(enforceRunnable)
        handler.removeCallbacks(pulseReleaseRunnable)
        unmuteStreams()
    }

    /**
     * Momentarily mute all streams for a blocked notification, without forcing a long-running state.
     */
    fun pulseMute() {
        muteStreams()
        if (!keepMuted) {
            handler.removeCallbacks(pulseReleaseRunnable)
            handler.postDelayed(pulseReleaseRunnable, PULSE_RELEASE_DELAY_MS)
        }
    }

    fun releasePulseImmediately() {
        if (keepMuted) {
            return
        }
        handler.removeCallbacks(pulseReleaseRunnable)
        unmuteStreams()
    }

    private fun muteStreams() {
        val context = MyApplication.context
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                streams.forEach {
                    audioManager.adjustStreamVolume(it, AudioManager.ADJUST_MUTE, 0)
                }
            } else {
                streams.forEach {
                    audioManager.setStreamMute(it, true)
                }
            }
            hasAppliedMute = true
            Timber.tag("AudioSilencer").d("Streams muted (persistent=$keepMuted)")
        } catch (t: Throwable) {
            Timber.tag("AudioSilencer").e(t, "Unable to mute streams")
        }
    }

    private fun unmuteStreams() {
        if (!hasAppliedMute) {
            return
        }
        val context = MyApplication.context
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                streams.forEach {
                    audioManager.adjustStreamVolume(it, AudioManager.ADJUST_UNMUTE, 0)
                }
            } else {
                streams.forEach {
                    audioManager.setStreamMute(it, false)
                }
            }
            Timber.tag("AudioSilencer").d("Streams unmuted")
        } catch (t: Throwable) {
            Timber.tag("AudioSilencer").e(t, "Unable to unmute streams")
        } finally {
            hasAppliedMute = false
        }
    }
}


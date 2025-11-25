package com.projects.allnotificationblocker.blockthemall.Utilities

import android.content.Context
import com.projects.allnotificationblocker.blockthemall.Activities.Rules.RulesManager
import com.projects.allnotificationblocker.blockthemall.Utilities.Constants.RULE_BLOCK_ALL

object BlockingAudioCoordinator {
    fun syncWithRules(context: Context, manager: RulesManager?) {
        if (manager == null) return
        val shouldMute = manager.isBlockAllEnabled || manager.hasCustomEnabledValidRules(RULE_BLOCK_ALL)
        if (shouldMute) {
            AudioSilencer.enforcePersistentMute()
        } else {
            AudioSilencer.releasePersistentMute()
        }
    }
}


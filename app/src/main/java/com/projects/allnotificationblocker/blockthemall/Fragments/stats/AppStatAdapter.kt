package com.projects.allnotificationblocker.blockthemall.Fragments.stats

import android.content.*
import android.content.pm.*
import android.graphics.drawable.*
import android.util.Log
import android.view.*
import androidx.fragment.app.*
import androidx.recyclerview.widget.*
import com.bumptech.glide.*
import com.projects.allnotificationblocker.blockthemall.Activities.*
import com.projects.allnotificationblocker.blockthemall.Fragments.stats.AppStatAdapter.*
import com.projects.allnotificationblocker.blockthemall.databinding.*
import com.projects.allnotificationblocker.blockthemall.domain.*
import kotlin.collections.filter
import kotlin.collections.toMutableList

class AppStatAdapter(
    private val fragment: Fragment,
    apps: MutableList<AppStat>,
): RecyclerView.Adapter<AppStatVH>() {
    var appStat = apps
        set(value) {
            field = value
            filter()
        }

    private var filteredAppStat =apps


    var userFilter: String? = null
        set(value) {
            field = value
            filter()
        }
    fun filter() {
        filteredAppStat = if (userFilter.isNullOrEmpty()) {
            appStat
        } else {
            appStat.filter { it.appName.contains(userFilter!!, ignoreCase = true) }
                .toMutableList()
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): AppStatVH {
        val binding =
            ListItemStatBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return AppStatVH(binding)
    }

    override fun getItemCount(): Int {
        return filteredAppStat.size
    }

    override fun onBindViewHolder(holder: AppStatVH, pos: Int) {
        val appInfo = filteredAppStat[pos]
        val viewHolder = holder
        val context = fragment.requireContext()
        viewHolder.binding.apply {
            textViewAppName.text = appInfo.appName
            textViewAppRules.text = appInfo.notificationBlockedCount.toString()
            percentTv.text = String.format("%.2f", appInfo.notificationPercentage) + "%"
            var appIcon: Drawable? = null
            try {
                appIcon =
                    context.packageManager.getApplicationIcon(appInfo.packageName)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            Glide.with(fragment).load(appIcon).into(imageViewAppIcon)
            cardView.setOnClickListener {
                val intent = Intent(context, AppNotificationActivity::class.java)
                intent.putExtra(AppNotificationActivity.PACKAGE_EXTRA, appInfo.packageName)
                intent.putExtra(AppNotificationActivity.NAME_EXTRA, appInfo.appName)
                context.startActivity(intent)
            }
        }

    }


    inner class AppStatVH internal constructor(val binding: ListItemStatBinding):
        RecyclerView.ViewHolder(binding.root)
}
package com.projects.allnotificationblocker.blockthemall.Activities.Home

import android.content.*
import android.view.*
import android.widget.*
import androidx.annotation.*
import androidx.appcompat.content.res.*
import androidx.fragment.app.*
import com.google.android.material.tabs.*
import com.projects.allnotificationblocker.blockthemall.Fragments.Applications.*
import com.projects.allnotificationblocker.blockthemall.Fragments.Notifications.*
import com.projects.allnotificationblocker.blockthemall.Fragments.stats.*
import com.projects.allnotificationblocker.blockthemall.R
import com.projects.allnotificationblocker.blockthemall.Utilities.*
import timber.log.*

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */

class SectionsPagerAdapter(private val mContext: Context, fm: FragmentManager, var mode: Int):
    FragmentPagerAdapter(fm) {
    private val imageResId =
        intArrayOf(R.drawable.ic_notifications2, R.drawable.tab_apps, R.drawable.bar_chart)
    private val imageResId1 = intArrayOf(R.drawable.tab_apps)
    lateinit var notificationsFragment: NotificationsFragment
    var currentPos: Int = 0
    var applicationsFragment: ApplicationsFragment? = null
        private set
    var statisticsFragment: StatisticsFragment? = null
        private set

    init {
        if (mode == Constants.MODE_HOMEPAGE) {
            notificationsFragment = NotificationsFragment.newInstance()
            applicationsFragment = ApplicationsFragment.newInstance(mode)
            statisticsFragment = StatisticsFragment.newInstance(mode)
        } else {
            applicationsFragment = ApplicationsFragment.newInstance(mode)
            statisticsFragment = StatisticsFragment.newInstance(mode)
        }
    }

    override fun getItem(position: Int): Fragment {
        currentPos = position
        return if (mode == Constants.MODE_HOMEPAGE) {
            when (position) {
                0 -> notificationsFragment
                1 -> applicationsFragment!!
                2 -> statisticsFragment!!
                else -> applicationsFragment!!
            }
        } else {
            when (position) {
                0 -> applicationsFragment!!
                1 -> statisticsFragment!!
                else -> applicationsFragment!!
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return if (mode == Constants.MODE_HOMEPAGE) {
            mContext.resources.getString(TAB_TITLES[position])
        } else {
            mContext.resources.getString(TAB_TITLES1[position])
        }
    }

    override fun getCount(): Int {
        return if (mode == Constants.MODE_HOMEPAGE) {
            TAB_TITLES.size
        } else {
            TAB_TITLES1.size
        }
    }

    fun updateBackground(tab: TabLayout.Tab, position: Int, selecetedPos: Int) {
        //tag("AppInfo").d("position: %d, currentPos: %d", position, currentPos);
        val view = tab.customView
        if (view == null) {
            Timber.tag("AppInfo").d("view == null")
            return
        }

        val tv = view.findViewById<TextView>(R.id.text_view)
        val img = view.findViewById<ImageView>(R.id.image_view)
        val ll = view.findViewById<LinearLayout>(R.id.LL)

        if (position == selecetedPos) {
            tv.setTextColor(mContext.getColor(R.color.blue))
            img.setColorFilter(mContext.getColor(R.color.blue))
            ll.background =
                AppCompatResources.getDrawable(mContext, R.drawable.tab_white_bg_rounded)
        } else {
            tv.setTextColor(mContext.getColor(R.color.white))
            img.setColorFilter(mContext.getColor(R.color.white))
            ll.setBackgroundColor(mContext.getColor(android.R.color.transparent))
        }
    }


    fun getTabView(position: Int): View {
        // Given you have a custom layout in `res/layout/custom_tab.xml` with a TextView and ImageView
        val view = LayoutInflater.from(mContext).inflate(R.layout.tab_custom_layout, null)
        val tv = view.findViewById<TextView>(R.id.text_view)
        val img = view.findViewById<ImageView>(R.id.image_view)
        view.findViewById<LinearLayout?>(R.id.LL)
        if (mode == Constants.MODE_HOMEPAGE) {
            tv.setText(TAB_TITLES[position])
            img.setImageResource(imageResId[position])
        } else {
            tv.setText(TAB_TITLES1[position])
            img.setImageResource(imageResId1[position])
        }

        return view
    }

    companion object {
        @StringRes
        private val TAB_TITLES = intArrayOf(
            R.string.notifications,
            R.string.applications,
            R.string.statistics,
        )
        private val TAB_TITLES1 = intArrayOf(R.string.applications)
    }
}
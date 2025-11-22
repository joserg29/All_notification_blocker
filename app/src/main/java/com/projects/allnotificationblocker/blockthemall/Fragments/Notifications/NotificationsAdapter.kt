package com.projects.allnotificationblocker.blockthemall.Fragments.Notifications

import android.app.*
import android.app.PendingIntent.*
import android.content.*
import android.content.pm.*
import android.view.*
import android.widget.*
import androidx.annotation.*
import androidx.cardview.widget.*
import androidx.recyclerview.widget.*
import com.bumptech.glide.*
import com.mikhaellopez.circularimageview.*
import com.projects.allnotificationblocker.blockthemall.Application.*
import com.projects.allnotificationblocker.blockthemall.Application.MyApplication.Companion.getAppInfo
import com.projects.allnotificationblocker.blockthemall.Application.MyApplication.Companion.getIntent
import com.projects.allnotificationblocker.blockthemall.Fragments.Notifications.NotificationsAdapter.*
import com.projects.allnotificationblocker.blockthemall.R
import com.projects.allnotificationblocker.blockthemall.Utilities.*
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*
import com.projects.allnotificationblocker.blockthemall.domain.*
import io.noties.markwon.*
import timber.log.*
import java.util.*

class NotificationsAdapter constructor(
    private val mContext: Context,
    notifications: MutableList<NotificationInfo>,
): RecyclerView.Adapter<NotificationViewHolder?>() {
    val markwon = Markwon.create(mContext)
    var userFilter : String? = null
        set(value) {
            field = value
            notifications=notifications
        }
    private var listener: NotificationsAdapterListener
    var notifications: MutableList<NotificationInfo> = notifications
        set(value) {
            field = value
            mNotifications = run {
                var filtered = LogUtil.logDuration("filtering") {
                    notifications.filter { it.hasTitle() && !(it.isWhatsApp && it.tag.isNullOrEmpty()) }
                }
                if (!userFilter.isNullOrEmpty()) {
                   filtered= filtered.filter {
                        it.title!!.contains(userFilter!!, ignoreCase = true) ||
                                it.text!!.contains(userFilter!!, ignoreCase = true) ||
                                it.packageName.contains(userFilter!!, ignoreCase = true)
                    }
                }
                val grouped =
                    LogUtil.logDuration("grouping") { filtered.groupBy { it.tag + it.packageName } }
                val result = LogUtil.logDuration("mapping") {
                    grouped.map { (_, infos) ->
                        infos[0].copy(
                            text = NotificationsUtil.formatNotificationsAsMarkdown(infos),
                            title = NotificationsUtil.getGroupTitle(infos),
                            number = infos.size
                        )
                    }
                }
                result.toMutableList()
            }

        }
    private var mNotifications = mutableListOf<NotificationInfo>()


    init {
        if (mContext is NotificationsAdapterListener) {
            listener = mContext as NotificationsAdapterListener
        } else {
            throw RuntimeException(
                mContext.toString()
                        + " must implement NotificationsAdapterListener"
            )
        }
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): NotificationViewHolder {
        val itemView = LayoutInflater
            .from(viewGroup.context)
            .inflate(R.layout.list_item_notification, viewGroup, false)
        return NotificationViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return mNotifications.size
    }

    override fun onBindViewHolder(notificationViewHolder: NotificationViewHolder, pos: Int) {
        val myNotification = mNotifications[pos]
        Timber.tag("AppInfo")
            .d("myNotification.getPackageName(): %s", myNotification.packageName)

        val appInfo = getAppInfo(myNotification.packageName)
        appNotification(appInfo, notificationViewHolder, myNotification)
    }

    private fun setMoreButton(viewHolder: NotificationViewHolder) {
        viewHolder.imageButtonMore!!.setOnClickListener(object:
            View.OnClickListener {
            override fun onClick(view: View?) {
                //init the wrapper with style
                val wrapper: Context = ContextThemeWrapper(mContext, R.style.PopupMenu)
                //creating a popup menu
                val popup = PopupMenu(wrapper, viewHolder.imageButtonMore)
                //inflating menu from xml resource
                popup.inflate(R.menu.apps_menu)
                //adding click listener
                popup.setOnMenuItemClickListener(object: PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem): Boolean {
                        if (item.itemId == R.id.custom_rule) { //handle menu1 click
                            //Toast.makeText(mContext, "Setting clicked", Toast.LENGTH_SHORT).show();
                            //listener.customRules()
                        }
                        return false
                    }
                })
                //displaying the popup
                popup.show()
            }
        })
    }

    fun applyMarkdownToTextView(textView: TextView, text: String) {
        LogUtil.logDuration("markdown") {
            // Parse markdown to commonmark-java Node
            val node = markwon.parse(text)
            // Create styled text from parsed Node
            val markdown = markwon.render(node)
            // Use it on the provided TextView
            markwon.setParsedMarkdown(textView, markdown)
        }
    }

    private fun appNotification(
        appInfo: AppInfo?,
        viewHolder: NotificationViewHolder,
        notification: NotificationInfo,
    ) {
        LogUtil.logDuration("appNotification") {
            // Subtext
            if (notification.hasSubText()) {
                viewHolder.textViewSubtext!!.text = notification.subText?.takeMax(35)
            } else {
                viewHolder.textViewSubtext!!.visibility = View.GONE
            }
            // Title
            applyMarkdownToTextView(viewHolder.textViewCount!!, notification.title!!)
            // Text
            viewHolder.textViewTimestamp!!.text =
                DateTimeUtil.toHrMinAmPm(notification.timestamp!!)
            // Timestamp
            viewHolder.buttonCall!!.visibility = View.GONE//todo remove this
            setMoreButton(viewHolder)
            // Icon
            val app = appInfo ?: getAppInfo(notification.packageName)

            if (app == null) {
                viewHolder.imageViewIcon!!.setImageResource(R.drawable.android_24dp)
                viewHolder.textViewAppName!!.text = mContext.getString(R.string.app_not_found)
                return
            }
            applyMarkdownToTextView(viewHolder.textViewText!!, notification.text!!)
            var appDrawable = try {
                mContext.packageManager.getApplicationIcon(app.packageName)
            } catch (_: PackageManager.NameNotFoundException) {
                null
            }
            MyApplication.getIcon(app.packageName)
            if (appDrawable != null) {
                Glide.with(mContext).load(appDrawable)
                    .into(viewHolder.imageViewIcon!!)
            } else {
                Glide.with(mContext).load(R.drawable.android_24dp)
                    .into(viewHolder.imageViewIcon!!)
            }
            viewHolder.textViewAppName!!.text = app.appName
            val key = notification.packageName + "$#" + notification.postTime
            viewHolder.rootLayout.setCardBackgroundColor(mContext.getColor(getCardColor(notification.priority)))
            val pi = getIntent(key)

            if (pi == null) {
                viewHolder.linearLayout2!!.visibility = View.GONE
                return
            }
            viewHolder.imageViewIcon2!!.setVisibility(View.VISIBLE)
            viewHolder.textViewAppName2!!.visibility = View.VISIBLE

            if (appDrawable != null) {
                Glide.with(mContext).load(appDrawable)
                    .into(viewHolder.imageViewIcon2!!)
            } else {
                Glide.with(mContext).load(R.drawable.appicon1)
                    .into(viewHolder.imageViewIcon2!!)
            }

            viewHolder.imageViewIcon2!!.setOnClickListener(View.OnClickListener { view: View? ->
                try {
                    pi.send()
                    listener.removeNotification(notification)
                } catch (e: CanceledException) {
                    e.printStackTrace()
                }
            })
            viewHolder.textViewAppName2!!.text =
                mContext.getString(R.string.open_in) + app.appName
            //genericViewHolder.buttonCall.setCompoundDrawables(MyApplication.getAppIcon(appInfo.getPackageName()), null, null, null);
            viewHolder.textViewAppName2!!.setOnClickListener(View.OnClickListener { view: View? ->
                try {
                    pi.send()
                    listener.removeNotification(notification)
                } catch (e: CanceledException) {
                    e.printStackTrace()
                }
            })
        }

    }
    @ColorRes
    fun getCardColor(priority: Int): Int {
        return when (priority) {
            Notification.PRIORITY_LOW -> R.color.green
            Notification.PRIORITY_DEFAULT -> R.color.light_orange
            else -> R.color.dark_orange
        }

    }



    interface NotificationsAdapterListener {
        val notificationsList: MutableList<NotificationInfo?>?

        fun removeNotification(myNotification: NotificationInfo?)
    }

    class NotificationViewHolder internal constructor(v: View): RecyclerView.ViewHolder(v) {
        var imageViewIcon: CircularImageView? = null
        var textViewTimestamp: TextView? = null
        var textViewText: TextView? = null
        var textViewTitle: TextView? = null
        var textViewAppName2: TextView? = null

        var textViewCount: TextView? = null
        var imageViewIcon2: CircularImageView? = null

        var textViewAppName: TextView? = null
        var textViewSubtext: TextView? = null
        var buttonCall: Button? = null
        var linearLayout2: LinearLayout? = null
        var imageButtonMore: ImageButton? = null
        var linearLayoutTopBar: LinearLayout? = null
        var linearLayoutNotificationBody: LinearLayout? = null

        lateinit var rootLayout: CardView

        init {
            initView(v)
        }

        private fun initView(itemView: View) {
            imageViewIcon = itemView.findViewById<CircularImageView>(R.id.image_view_app_icon)
            textViewTimestamp = itemView.findViewById<TextView>(R.id.text_view_timestamp)
            textViewText = itemView.findViewById<TextView>(R.id.text_view_text)
            textViewTitle = itemView.findViewById<TextView>(R.id.text_view_title)
            imageButtonMore = itemView.findViewById<ImageButton>(R.id.image_button_more)
            textViewAppName = itemView.findViewById<TextView>(R.id.text_view_app_name)
            textViewSubtext = itemView.findViewById<TextView>(R.id.text_view_subtext)
            buttonCall = itemView.findViewById<Button>(R.id.button_call)
            textViewAppName2 = itemView.findViewById<TextView>(R.id.text_view_app_name2)
            imageViewIcon2 = itemView.findViewById<CircularImageView>(R.id.image_view_app_icon2)
            linearLayout2 = itemView.findViewById<LinearLayout>(R.id.linear_layout2)
            rootLayout = itemView.findViewById<CardView>(R.id.root_layout)
            textViewCount = itemView.findViewById<TextView>(R.id.text_view_count)
            linearLayoutTopBar = itemView.findViewById<LinearLayout?>(R.id.linear_layout_top_bar)
            linearLayoutNotificationBody =
                itemView.findViewById<LinearLayout>(R.id.linear_layout_notification_body)
            linearLayoutNotificationBody!!.visibility = View.GONE
            textViewCount!!.setOnClickListener(View.OnClickListener { view: View? -> toggleBody() })
            textViewAppName!!.setOnClickListener(View.OnClickListener { view: View? -> toggleBody() })
            textViewSubtext!!.setOnClickListener(View.OnClickListener { view: View? -> toggleBody() })
            textViewTimestamp!!.setOnClickListener(View.OnClickListener { view: View? -> toggleBody() })
            imageViewIcon!!.setOnClickListener(View.OnClickListener { view: View? -> toggleBody() })
        }

        private fun toggleBody() {
            if (linearLayoutNotificationBody!!.visibility == View.VISIBLE) {
                linearLayoutNotificationBody!!.visibility = View.GONE
            } else {
                linearLayoutNotificationBody!!.visibility = View.VISIBLE
            }
        }
    }
}
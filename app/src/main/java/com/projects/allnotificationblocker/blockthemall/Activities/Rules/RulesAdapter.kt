package com.projects.allnotificationblocker.blockthemall.Activities.Rules

import android.content.*
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.*
import com.projects.allnotificationblocker.blockthemall.R
import com.projects.allnotificationblocker.blockthemall.Utilities.*
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*
import com.projects.allnotificationblocker.blockthemall.domain.WeekDays
import io.noties.markwon.Markwon
import timber.log.*
import java.util.Date

class RulesAdapter(val context: Context, var rules: MutableList<Rule>):
    RecyclerView.Adapter<RulesAdapter.ViewHolder?>() {
    private lateinit var listener: RulesAdapterListener

    init {
        if (context is RulesAdapterListener) {
            listener = context as RulesAdapterListener
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement RulesAdapterListener"
            )
        }
    }

    override fun getItemCount(): Int {
        return rules.size
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        val rule = rules[pos]
        val viewHolder = holder

        viewHolder.mRuletypeTextview!!.text = rule.getRuleTypeString(context)
        viewHolder.mRuletoTextview!!.text= rule.schedule.resString(Date(),context)
        weekdaysMarkDown(rule.schedule.weekDays,viewHolder.weekDaysTv!!)
        if (listener!!.mode == Constants.MODE_HOMEPAGE) {
            if (rule.isInactive()) {
                Timber.tag("AppInfo").d("Rule is still inactive")
                viewHolder.textViewStatus!!.visibility = View.VISIBLE
                viewHolder.textViewStatus!!.setText(R.string.inactive)
            }

            if (rule.isActive()) {
                Timber.tag("AppInfo").d("rule is active")
                viewHolder.textViewStatus!!.visibility = View.VISIBLE
                viewHolder.textViewStatus!!.setText(R.string.active)
            }


            if (rule.isExpired()) {
                Timber.tag("AppInfo").d("rule is expired")
                viewHolder.textViewStatus!!.visibility = View.VISIBLE
                viewHolder.textViewStatus!!.setText(R.string.expired)
            }
        } else {

            /* todo ui
            genericViewHolder.mRulefromTextview!!.text = toReadableFormat1(rule.from)
            genericViewHolder.mRuletoTextview!!.text = toReadableFormat1(rule.to)
            genericViewHolder.textViewDuration!!.text = getDuration(rule.from, rule.to)
1*/
            viewHolder.textViewStatus!!.visibility = View.GONE
        }

        viewHolder.mMoreImagebutton!!.setOnClickListener(object: View.OnClickListener {
            override fun onClick(view: View?) {
                //init the wrapper with style
                val wrapper: Context = ContextThemeWrapper(context, R.style.PopupMenu)

                //creating a popup menu
                val popup = PopupMenu(wrapper, viewHolder.mMoreImagebutton)
                //inflating menu from xml resource
                popup.inflate(R.menu.rules_menu)
                //adding click listener
                popup.setOnMenuItemClickListener(object: PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem): Boolean {
                        if (item.itemId == R.id.remove) { //handle menu1 click
                            listener.removeRule(rule)
                        } else if (item.itemId == R.id.edit) {
                            listener.onEditRuleClicked(rule)
                        }
                        return false
                    }
                })
                popup.show()
            }
        })
    }

private fun weekdaysMarkDown(days: WeekDays?, weekDaysTextView: TextView) {
    if (days == null) {
        weekDaysTextView.text = ""
        return
    }
    val labels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val flags = listOf(
        days.sunday, days.monday, days.tuesday,
        days.wednesday, days.thursday, days.friday,
        days.saturday
    )
    val markdown = labels.zip(flags)
        .joinToString(" ") { (label, selected) ->
            if (selected) "`$label`" else label
        }
    val markwon = Markwon.create(context)
    markwon.setMarkdown(weekDaysTextView, markdown)
}

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_item_rule, viewGroup, false)
        return ViewHolder(itemView)
    }

    interface RulesAdapterListener {
        fun removeRule(rule: Rule)

        fun onEditRuleClicked(rule: Rule)

        val mode: Int
    }

    class ViewHolder internal constructor(v: View): RecyclerView.ViewHolder(v) {
        var mRuletypeTextview: TextView? = null
        var mRuletoTextview: TextView? = null
        var mMoreImagebutton: ImageButton? = null
        var textViewStatus: TextView? = null
        var weekDaysTv: TextView? = null


        init {
            initView(v)
        }

        fun initView(itemView: View) {
            mRuletypeTextview = itemView.findViewById<TextView>(R.id.textview_ruletype)
            mRuletoTextview = itemView.findViewById<TextView>(R.id.textview_ruleto)
            mMoreImagebutton = itemView.findViewById<ImageButton>(R.id.imagebutton_more)
            textViewStatus = itemView.findViewById<TextView>(R.id.textview_status)
            weekDaysTv = itemView.findViewById<TextView>(R.id.textview_duration)
        }
    }
}
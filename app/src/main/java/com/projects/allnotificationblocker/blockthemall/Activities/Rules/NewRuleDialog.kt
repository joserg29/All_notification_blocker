package com.projects.allnotificationblocker.blockthemall.Activities.Rules

import android.app.*
import android.content.*
import android.graphics.*
import android.os.*
import android.view.*
import androidx.core.graphics.drawable.*
import androidx.fragment.app.DialogFragment
import com.google.android.material.datepicker.*
import com.google.android.material.timepicker.*
import com.projects.allnotificationblocker.blockthemall.*
import com.projects.allnotificationblocker.blockthemall.Utilities.*
import com.projects.allnotificationblocker.blockthemall.databinding.*
import com.projects.allnotificationblocker.blockthemall.domain.*
import java.util.*


class NewRuleDialog: DialogFragment() {

    private lateinit var binding: DialogNewRuleBinding
    var listener: DialogListener? = null
    var pkey: Int = -1
    var mode: Int = 0
    var selectedDateRange: DateRange? = null
    private val schedule = Schedule()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DialogNewRuleBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun initView() {
        binding.buttonYes.setOnClickListener {
            if (pkey == -1) {
                listener!!.saveNewRule(schedule)
            } else {
                listener!!.updateRule(pkey, schedule)
            }
            dismiss()
        }
        binding.buttonNo.setOnClickListener {
            dismiss()
        }
        binding.buttonStart.setOnClickListener {
            selectTime(true)
        }
        binding.buttonEnd.setOnClickListener {
            selectTime(false)
        }
        binding.repeatCheckBox.setOnCheckedChangeListener { _, isChecked ->
            rebind()
        }

        binding.dateRange.setOnClickListener {
            selectDateRange()
        }
        val weekDaysTb = listOf(
            binding.sundayTb,
            binding.mondayTb,
            binding.tuesdayTb,
            binding.wednesdayTb,
            binding.thursdayTb,
            binding.fridayTb,
            binding.saturdayTb
        )
        weekDaysTb.forEachIndexed { index, checkBox ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                schedule.setWeekDays(index, isChecked)
                rebind()
            }
        }

    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DialogListener) {
            listener = context as DialogListener
        } else {
            throw RuntimeException(
                context.toString() + "must implement DialogListener"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // binding = null todo check if this is needed
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null) {
            pkey = requireArguments().getInt("pkey")
            mode = requireArguments().getInt("mode")
        }
        Objects.requireNonNull<Window?>(Objects.requireNonNull<Dialog?>(dialog).window)
            .setBackgroundDrawable(
                Color.TRANSPARENT.toDrawable()
            )
        initView()
        rebind()
        Calendar.getInstance()
    }


    private fun selectDateRange() {
        // Build the picker
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR).setTitleText("Select Date Range")
            .setTheme(R.style.ThemeOverlay_App_DatePicker).build()
        // Show it
        dateRangePicker.show(childFragmentManager, "DATE_RANGE_PICKER")
        // Listen for positive click (user tapped “OK”)
        dateRangePicker.addOnPositiveButtonClickListener { selection: androidx.core.util.Pair<Long, Long> ->
            // selection.first = startMillis, selection.second = endMillis
            selectedDateRange = schedule.getDateRangeMillis(
                selection.first, selection.second
            )
            // If you need to clear time components (set to 00:00:00):

            // Update your UI or do whatever you need
            rebind()
        }
    }

    private fun selectTime(isStart: Boolean) {
        // Default hour and minute
        val (hour, min) = (if (isStart) schedule.getStartTime()
        else schedule.getEndTime()) ?: DateTimeUtil.hoursAndMinutes(Date())

        // Build the MaterialTimePicker
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H) // or CLOCK_12H for AM/PM
            .setHour(hour).setMinute(min)
            .setTitleText(if (isStart) getString(R.string.select_start_time) else getString(R.string.select_end_time))
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK).build()

        // Show the picker
        picker.show(childFragmentManager, "time_picker")

        // Handle time selection
        picker.addOnPositiveButtonClickListener {
            if (isStart) {
                schedule.setStartTime(picker.hour, picker.minute)
            } else {
                schedule.setEndTime(picker.hour, picker.minute)
            }
            rebind()
        }
    }


    private fun rebind() {
        binding.dateRange.text = if (selectedDateRange != null) {
            if (schedule.isOutDated(Date())) {
                getString(R.string.expired)
            } else {
                selectedDateRange!!.getDuration(Date(), requireContext())
            }
        } else {
            getString(R.string.select_date)
        }

        schedule.timeRange?.getStartString()?.let {
            binding.startTimeTv.text = it
        } ?: {
            binding.startTimeTv.text = getString(R.string.select_start_time)
        }
        schedule.timeRange?.getEndString()?.let {
            binding.endTimeTv.text = it
        } ?: {
            binding.endTimeTv.text = getString(R.string.select_end_time)
        }

        if (schedule.timeRange?.isComplete() == true) {
            binding.timeDurationTv.visibility = View.VISIBLE
            if (schedule.timeRange!!.isInvalid()) {
                binding.timeDurationTv.text = getString(R.string.invalid_time_range)
            } else {
                binding.timeDurationTv.text = schedule.timeRange!!.getTimeDuration()
            }
        } else {
            binding.timeDurationTv.visibility = View.GONE
        }
        val repeat = binding.repeatCheckBox.isChecked
        if (schedule.isValid(Date())) {
            binding.buttonYes.isEnabled = true
            if (selectedDateRange == null) {
                if (!repeat) {
                    val range = if (schedule.weekDays?.anyWeekDaysSelected() == true) {
                        schedule.weekDays!!.getNextDateRange(Date())!!
                    } else {
                        if (schedule.timeRange!!.isOutDated())
                            DateRange.getTomorrowDate()
                        else
                            DateRange.getTodayDate()
                    }
                    schedule.dateRange = range
                } else {
                    schedule.dateRange = null
                }
            } else {
                binding.repeatCheckBox.isChecked = true
                binding.repeatCheckBox.isEnabled = false
                schedule.dateRange = selectedDateRange
            }
        } else {
            binding.buttonYes.isEnabled = false
        }
        binding.resTv.text = schedule.resString(Date(), requireContext())

    }

    interface DialogListener {
        fun saveNewRule(schedule: Schedule)
        fun updateRule(pkey: Int, schedule: Schedule)
    }


    companion object {
        @JvmStatic fun newInstance(pkey: Int, mode: Int): NewRuleDialog {
            val frag = NewRuleDialog()
            val b = Bundle()
            b.putInt("pkey", pkey)
            b.putInt("mode", mode)
            frag.arguments = b
            return frag
        }
    }
}

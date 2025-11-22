package com.projects.allnotificationblocker.blockthemall.domain

import junit.framework.TestCase.assertEquals
import org.junit.*
import org.junit.Assert.assertNull
import java.util.Calendar
import java.util.Date


class WeekDaysTest {

    private lateinit var baseDate: Date
    private lateinit var calendar: Calendar

    @Before
    fun setUp() {
        calendar = Calendar.getInstance().apply {
            // normalize to midnight
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        baseDate = calendar.time
    }

    private fun datePlus(days: Int): Date {
        return Calendar.getInstance().apply {
            time = baseDate
            add(Calendar.DAY_OF_YEAR, days)
        }.time
    }

    @Test
    fun `no weekdays selected returns null`() {
        val ws = WeekDays()
        val result = ws.getNextDateRange(baseDate)
        assertNull(result)
    }
    @Test
    fun `all weekdays selected returns today`() {
        // Suppose today is Thursday
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
        baseDate = calendar.time

        val ws = WeekDays(
            sunday = true,
            monday = true,
            tuesday = true,
            wednesday = true,
            thursday = true,
            friday = true,
            saturday = true
        )
        val range = ws.getNextDateRange(baseDate)!!
        assertEquals(calendar.get(Calendar.DAY_OF_MONTH), range.startDay)
        assertEquals(calendar.get(Calendar.MONTH) + 1, range.startMonth)
        assertEquals(calendar.get(Calendar.YEAR), range.startYear)
    }

    @Test
    fun `single weekday today returns today`() {
        // Suppose today is Thursday
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
        baseDate = calendar.time

        val ws = WeekDays(thursday = true)
        val range = ws.getNextDateRange(baseDate)!!
        assertEquals(calendar.get(Calendar.DAY_OF_MONTH), range.startDay)
        assertEquals(calendar.get(Calendar.MONTH) + 1, range.startMonth)
        assertEquals(calendar.get(Calendar.YEAR), range.startYear)
        // end should be same
        assertEquals(range.startDay, range.endDay)
    }

    @Test
    fun `next active is tomorrow`() {
        // Today is Friday, active only on Saturday
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
        baseDate = calendar.time

        val ws = WeekDays(saturday = true)
        val range = ws.getNextDateRange(baseDate)!!
        val expected = datePlus(1)
        calendar.time = expected

        assertEquals(calendar.get(Calendar.DAY_OF_MONTH), range.startDay)
        assertEquals(calendar.get(Calendar.MONTH) + 1, range.startMonth)
        assertEquals(calendar.get(Calendar.YEAR), range.startYear)
    }

    @Test
    fun `contiguous run spans multiple days`() {
        // Activate Monday, Tuesday, Wednesday
        val ws = WeekDays(monday = true, tuesday = true, wednesday = true)
        // Start on Monday
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        baseDate = calendar.time

        val range = ws.getNextDateRange(baseDate)!!
        // Should run Mon→Wed
        assertEquals(calendar.get(Calendar.DAY_OF_MONTH), range.startDay)
        assertEquals(calendar.get(Calendar.MONTH) + 1, range.startMonth)

        // Compute Wednesday
        val wed = Calendar.getInstance().apply {
            time = baseDate
            add(Calendar.DAY_OF_YEAR, 2)
        }
        assertEquals(wed.get(Calendar.DAY_OF_MONTH), range.endDay)
        assertEquals(wed.get(Calendar.MONTH) + 1, range.endMonth)
    }


    @Test
    fun `start search window limit returns null`() {
        // Activate only on a day not in the next 14 days (impossible, but for test)
        // e.g. no days at all
        val ws = WeekDays()
        val result = ws.getNextDateRange(baseDate)
        assertNull(result)
    }
}

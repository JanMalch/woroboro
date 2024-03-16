package io.github.janmalch.woroboro.ui.reminder

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.util.Locale

class ReminderListTest {

    private val locale = Locale.GERMAN

    @Test
    fun `joinRangesToString should work for a single day`() {
        assertEquals(
            "Mo.",
            setOf(DayOfWeek.MONDAY).joinRangesToString(locale)
        )
    }

    @Test
    fun `joinRangesToString should work for an entire week`() {
        assertEquals(
            "Mo. – So.",
            DayOfWeek.entries.toSet().joinRangesToString(locale)
        )
    }

    @Test
    fun `joinRangesToString should work for every other day`() {
        assertEquals(
            "Mo., Mi., Fr., So.",
            setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SUNDAY
            ).joinRangesToString(locale)
        )
    }

    @Test
    fun `joinRangesToString should work for a single range`() {
        assertEquals(
            "Mo. – Fr.",
            setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            ).joinRangesToString(locale)
        )
    }

    @Test
    fun `joinRangesToString should work for two ranges`() {
        assertEquals(
            "Mo. – Mi., Fr. – So.",
            setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY
            ).joinRangesToString(locale)
        )
    }

    @Test
    fun `joinRangesToString should work for range and single Sunday`() {
        assertEquals(
            "Mo. – Mi., So.",
            setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.SUNDAY
            ).joinRangesToString(locale)
        )
    }

    @Test
    fun `joinRangesToString should work for range and single Saturday`() {
        assertEquals(
            "Mo. – Mi., Sa.",
            setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.SATURDAY
            ).joinRangesToString(locale)
        )
    }
}
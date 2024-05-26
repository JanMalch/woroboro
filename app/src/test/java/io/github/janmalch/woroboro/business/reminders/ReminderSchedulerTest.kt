package io.github.janmalch.woroboro.business.reminders

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.ZoneId
import kotlin.time.Duration.Companion.days
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderSchedulerTest {

    private val zoneId = ZoneId.of("Europe/Berlin")

    private val ten = LocalTime.of(10, 0)
    private val thirteen = LocalTime.of(13, 0)
    private val twelve = LocalTime.of(12, 0)

    private val today = LocalDate.of(2023, Month.DECEMBER, 9)

    private val now = LocalDateTime.of(today, twelve).toInstant()

    @Test
    fun `closestInstantBy should use the same day if time is in the future`() {
        val weekdays = setOf(DayOfWeek.SATURDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)

        val actual =
            closestInstantBy(weekdays = weekdays, time = thirteen, zoneId = zoneId, now = now)
        val expected = LocalDateTime.of(today, thirteen).toInstant()
        assertEquals(expected, actual)
    }

    @Test
    fun `closestInstantBy should use another day if time is in the past`() {
        val weekdays = setOf(DayOfWeek.SATURDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)

        val actual =
            closestInstantBy(
                weekdays = weekdays,
                time = ten,
                zoneId = zoneId,
                now = now,
            )
        val expected =
            LocalDateTime.of(
                    // next Tuesday
                    LocalDate.of(2023, Month.DECEMBER, 12),
                    ten
                )
                .toInstant()
        assertEquals(expected, actual)
    }

    @Test
    fun `closestInstantBy should use next week's day if time is in the past`() {
        val weekdays = setOf(DayOfWeek.SATURDAY)

        val actual =
            closestInstantBy(
                weekdays = weekdays,
                time = ten,
                zoneId = zoneId,
                now = now,
            )
        val expected = LocalDateTime.of(LocalDate.of(2023, Month.DECEMBER, 16), ten).toInstant()
        assertEquals(expected, actual)
    }

    @Test
    fun `nextInstantOf should use the same day if time is in the future`() {
        val actual =
            nextInstantOf(
                dayOfWeek = DayOfWeek.SATURDAY,
                time = thirteen,
                zoneId = zoneId,
                now = now
            )
        val expected = LocalDateTime.of(today, thirteen).toInstant()
        assertEquals(expected, actual)
    }

    @Test
    fun `nextInstantOf should use another day if time is in the past`() {
        val actual =
            nextInstantOf(dayOfWeek = DayOfWeek.SATURDAY, time = ten, zoneId = zoneId, now = now)
        val expected =
            LocalDateTime.of(
                    // next Saturday
                    LocalDate.of(2023, Month.DECEMBER, 16),
                    ten
                )
                .toInstant()
        assertEquals(expected, actual)
    }

    @Test
    fun `shortestDurationBetween should correctly determine 1 day differences`() {
        assertEquals(1.days, shortestDurationBetween(setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)))
        assertEquals(1.days, shortestDurationBetween(setOf(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY)))
        assertEquals(1.days, shortestDurationBetween(setOf(DayOfWeek.SUNDAY, DayOfWeek.MONDAY)))
    }

    @Test
    fun `shortestDurationBetween should return at least 1 day for 2 day differences`() {
        assertTrue(shortestDurationBetween(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)) >= 1.days)
        assertTrue(shortestDurationBetween(setOf(DayOfWeek.THURSDAY, DayOfWeek.SATURDAY)) >= 1.days)
        assertTrue(shortestDurationBetween(setOf(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY)) >= 1.days)
    }

    @Test
    fun `shortestDurationBetween should correctly determine 4 day differences`() {
        assertTrue(shortestDurationBetween(setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)) >= 1.days)
        assertTrue(
            shortestDurationBetween(setOf(DayOfWeek.SATURDAY, DayOfWeek.WEDNESDAY)) >= 1.days
        )
    }

    @Test
    fun `shortestDurationBetween should correctly determine 7 day differences`() {
        DayOfWeek.entries.forEach { assertEquals(7.days, shortestDurationBetween(setOf(it))) }
    }

    private fun LocalDateTime.toInstant() = atZone(zoneId).toInstant()
}

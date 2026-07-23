package com.james.mathwakealarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class AlarmOccurrenceTest {
    private val zone = ZoneId.of("Australia/Brisbane")

    @Test
    fun schedulesSameDayWhenTimeIsStillAhead() {
        val now = ZonedDateTime.of(2026, 7, 23, 6, 0, 0, 0, zone)
        val alarm = defaultAlarm().copy(hour = 6, minute = 30, days = listOf(4))
        val next = AlarmScheduler.nextOccurrence(alarm, now)
        assertEquals(23, next.dayOfMonth)
        assertEquals(6, next.hour)
        assertEquals(30, next.minute)
    }

    @Test
    fun advancesToNextSelectedDayAfterTimePasses() {
        val now = ZonedDateTime.of(2026, 7, 23, 7, 0, 0, 0, zone)
        val alarm = defaultAlarm().copy(hour = 6, minute = 30, days = listOf(4, 5))
        val next = AlarmScheduler.nextOccurrence(alarm, now)
        assertEquals(24, next.dayOfMonth)
        assertEquals(5, next.dayOfWeek.value)
    }

    @Test
    fun skipOccurrenceMovesToFollowingSelectedDay() {
        val now = ZonedDateTime.of(2026, 7, 23, 6, 0, 0, 0, zone)
        val first = ZonedDateTime.of(2026, 7, 23, 6, 30, 0, 0, zone)
        val alarm = defaultAlarm().copy(
            hour = 6,
            minute = 30,
            days = listOf(4, 5),
            skipOccurrenceAt = first.toInstant().toEpochMilli()
        )
        val next = AlarmScheduler.nextOccurrence(alarm, now)
        assertTrue(next.isAfter(first))
        assertEquals(24, next.dayOfMonth)
    }
}

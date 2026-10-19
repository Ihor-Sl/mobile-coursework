package com.example.meditationapplication

import com.example.meditationapplication.data.model.Category
import com.example.meditationapplication.data.model.Meditation
import com.example.meditationapplication.data.repository.calculateStreak
import com.example.meditationapplication.service.ReminderScheduler
import com.example.meditationapplication.ui.components.formatClock
import com.example.meditationapplication.ui.components.secondsToMinutes
import com.example.meditationapplication.ui.components.ukPlural
import com.example.meditationapplication.ui.home.DayPart
import com.example.meditationapplication.ui.home.dayPartOf
import com.example.meditationapplication.ui.home.recommendationFor
import com.example.meditationapplication.ui.library.LibraryFilter
import com.example.meditationapplication.ui.library.applyFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class StreakTest {
    private val today = LocalDate.of(2026, 10, 18)

    @Test
    fun `приклад з курсової - 4 дні поспіль з 15 по 18 жовтня`() {
        val days = listOf(15, 16, 17, 18).map { LocalDate.of(2026, 10, it) }
        assertEquals(4, calculateStreak(days, today))
    }

    @Test
    fun `якщо сьогодні сесій ще не було, серія рахується від учора`() {
        val days = listOf(16, 17).map { LocalDate.of(2026, 10, it) }
        assertEquals(2, calculateStreak(days, today))
    }

    @Test
    fun `пропуск дня обриває серію`() {
        val days = listOf(14, 15, 17, 18).map { LocalDate.of(2026, 10, it) }
        assertEquals(2, calculateStreak(days, today))
    }

    @Test
    fun `без сесій учора і сьогодні серія нульова`() {
        assertEquals(0, calculateStreak(listOf(LocalDate.of(2026, 10, 10)), today))
        assertEquals(0, calculateStreak(emptyList(), today))
    }
}

class FormatTest {
    @Test
    fun `українські форми множини`() {
        val forms = listOf(1, 2, 4, 5, 11, 12, 14, 21, 22, 25, 101, 111)
            .map { ukPlural(it, "день", "дні", "днів") }
        assertEquals(
            listOf("день", "дні", "дні", "днів", "днів", "днів", "днів", "день", "дні", "днів", "день", "днів"),
            forms,
        )
    }

    @Test
    fun `секунди в хвилини`() {
        assertEquals(0, secondsToMinutes(0))
        assertEquals(1, secondsToMinutes(5))
        assertEquals(5, secondsToMinutes(300))
        assertEquals(10, secondsToMinutes(599))
    }

    @Test
    fun `формат таймера`() {
        assertEquals("10:00", formatClock(600_000))
        assertEquals("00:01", formatClock(1))
        assertEquals("00:00", formatClock(0))
    }
}

class HomeLogicTest {
    private fun meditation(id: String, category: Category = Category.SLEEP, favorite: Boolean = false) =
        Meditation(id, id, "", category, 300, "audio/$id.mp3", "images/$id.webp", favorite)

    @Test
    fun `привітання залежить від часу доби`() {
        assertEquals(DayPart.MORNING, dayPartOf(LocalTime.of(8, 0)))
        assertEquals(DayPart.DAY, dayPartOf(LocalTime.of(13, 0)))
        assertEquals(DayPart.EVENING, dayPartOf(LocalTime.of(21, 30)))
        assertEquals(DayPart.EVENING, dayPartOf(LocalTime.of(2, 0)))
    }

    @Test
    fun `рекомендація змінюється щодня і повторюється по колу`() {
        val catalog = listOf(meditation("a"), meditation("b"), meditation("c"))
        val day = LocalDate.of(2026, 10, 18)
        val first = recommendationFor(catalog, day)
        val next = recommendationFor(catalog, day.plusDays(1))
        assert(first != next)
        assertEquals(first, recommendationFor(catalog, day.plusDays(3)))
        assertNull(recommendationFor(emptyList(), day))
    }

    @Test
    fun `фільтри каталогу`() {
        val sleep = meditation("sleep", Category.SLEEP)
        val focus = meditation("focus", Category.FOCUS, favorite = true)
        val all = listOf(sleep, focus)
        assertEquals(all, applyFilter(LibraryFilter.ALL, all, listOf(focus)))
        assertEquals(listOf(focus), applyFilter(LibraryFilter.FAVORITES, all, listOf(focus)))
        assertEquals(listOf(sleep), applyFilter(LibraryFilter.SLEEP, all, listOf(focus)))
        assertEquals(emptyList<Meditation>(), applyFilter(LibraryFilter.BREATHING, all, listOf(focus)))
    }
}

class ReminderSchedulerTest {
    @Test
    fun `нагадування сьогодні, якщо час ще не настав`() {
        val now = LocalDateTime.of(2026, 10, 18, 7, 30)
        assertEquals(Duration.ofMinutes(30), ReminderScheduler.delayUntil(LocalTime.of(8, 0), now))
    }

    @Test
    fun `нагадування завтра, якщо час уже минув`() {
        val now = LocalDateTime.of(2026, 10, 18, 9, 0)
        assertEquals(Duration.ofHours(23), ReminderScheduler.delayUntil(LocalTime.of(8, 0), now))
    }
}

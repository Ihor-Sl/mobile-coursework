package com.example.meditationapplication.ui.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.meditationapplication.R
import com.example.meditationapplication.data.model.Category
import com.example.meditationapplication.data.model.Meditation
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ukrainian: Locale = Locale.forLanguageTag("uk")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMM, HH:mm", ukrainian)

@StringRes
fun Category.labelRes(): Int = when (this) {
    Category.SLEEP -> R.string.category_sleep
    Category.STRESS -> R.string.category_stress
    Category.FOCUS -> R.string.category_focus
    Category.BREATHING -> R.string.category_breathing
}

/** Секунди → хвилини з округленням; ненульова тривалість дає щонайменше 1 хв. */
fun secondsToMinutes(seconds: Int): Int =
    if (seconds <= 0) 0 else ((seconds + 30) / 60).coerceAtLeast(1)

/** Мілісекунди → «мм:сс» (секунди округлюються вгору, щоб відлік закінчувався на 00:00). */
fun formatClock(millis: Long): String {
    val totalSec = ((millis.coerceAtLeast(0) + 999) / 1000)
    return String.format(Locale.ROOT, "%02d:%02d", totalSec / 60, totalSec % 60)
}

/** Мілісекунди → «мм:сс» з округленням униз (поточна позиція в плеєрі). */
fun formatPosition(millis: Long): String {
    val totalSec = millis.coerceAtLeast(0) / 1000
    return String.format(Locale.ROOT, "%02d:%02d", totalSec / 60, totalSec % 60)
}

/** Вибір форми слова для української мови: 1 день, 2 дні, 5 днів. */
fun ukPlural(n: Int, one: String, few: String, many: String): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod10 == 1 && mod100 != 11 -> one
        mod10 in 2..4 && mod100 !in 12..14 -> few
        else -> many
    }
}

fun daysWord(n: Int) = ukPlural(n, "день", "дні", "днів")

@Composable
fun minutesText(seconds: Int): String =
    stringResource(R.string.minutes_short, secondsToMinutes(seconds))

/** «10 хв · Сон» */
@Composable
fun durationAndCategory(meditation: Meditation): String = stringResource(
    R.string.meta_separator,
    minutesText(meditation.durationSec),
    stringResource(meditation.category.labelRes()),
)

/** «Сьогодні, 21:10», «Вчора, 08:05» або «9 жовт., 22:40». */
@Composable
fun sessionDateText(startedAt: Long, today: LocalDate = LocalDate.now()): String {
    val dateTime = Instant.ofEpochMilli(startedAt).atZone(ZoneId.systemDefault())
    val time = dateTime.format(timeFormatter)
    return when (dateTime.toLocalDate()) {
        today -> stringResource(R.string.today_at, time)
        today.minusDays(1) -> stringResource(R.string.yesterday_at, time)
        else -> dateTime.format(dateTimeFormatter)
    }
}

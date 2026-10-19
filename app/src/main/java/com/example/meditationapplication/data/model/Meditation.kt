package com.example.meditationapplication.data.model

import kotlinx.serialization.Serializable

/** Категорія медитації (фільтри-чипи в каталозі). */
@Serializable
enum class Category { SLEEP, STRESS, FOCUS, BREATHING }

/**
 * Медитація з каталогу (доменна модель).
 * Поле [isFavorite] не зберігається в JSON — його заповнює репозиторій за таблицею favorites.
 */
data class Meditation(
    val id: String,
    val title: String,
    val description: String,
    val category: Category,
    val durationSec: Int,
    val audioFile: String,
    val imageFile: String,
    val isFavorite: Boolean,
)

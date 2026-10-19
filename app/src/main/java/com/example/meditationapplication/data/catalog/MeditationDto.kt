package com.example.meditationapplication.data.catalog

import com.example.meditationapplication.data.model.Category
import com.example.meditationapplication.data.model.Meditation
import kotlinx.serialization.Serializable

/** Повторює структуру одного об'єкта у файлі assets/meditations.json. */
@Serializable
data class MeditationDto(
    val id: String,
    val title: String,
    val description: String,
    val category: Category,
    val durationSec: Int,
    val audioFile: String,
    val imageFile: String,
)

fun MeditationDto.toMeditation(isFavorite: Boolean) = Meditation(
    id = id,
    title = title,
    description = description,
    category = category,
    durationSec = durationSec,
    audioFile = audioFile,
    imageFile = imageFile,
    isFavorite = isFavorite,
)

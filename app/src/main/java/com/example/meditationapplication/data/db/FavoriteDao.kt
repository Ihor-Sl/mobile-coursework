package com.example.meditationapplication.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Запис таблиці favorites. Медитація може бути в обраному не більше одного разу,
 * тому meditation_id є первинним ключем. Зв'язок з каталогом логічний (без FOREIGN KEY).
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    @ColumnInfo(name = "meditation_id") val meditationId: String,
    /** Час додавання, мс від 01.01.1970 (UTC); використовується для сортування. */
    @ColumnInfo(name = "added_at") val addedAt: Long,
)

@Dao
interface FavoriteDao {
    @Query("SELECT meditation_id FROM favorites ORDER BY added_at DESC")
    fun observeIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE meditation_id = :id")
    suspend fun remove(id: String)
}

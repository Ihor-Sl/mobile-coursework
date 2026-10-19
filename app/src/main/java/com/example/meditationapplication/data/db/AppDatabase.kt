package com.example.meditationapplication.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** База даних meditation.db з таблицями sessions і favorites. */
@Database(
    entities = [SessionEntity::class, FavoriteEntity::class],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val NAME = "meditation.db"

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, NAME).build()
    }
}

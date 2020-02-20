package com.jiwoolee.memoappchallenge.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Memo::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)

abstract class MemoDB: RoomDatabase() {
    abstract fun memoDao(): MemoDao

    companion object {
        private var INSTANCE: MemoDB? = null

        fun getInstance(context: Context): MemoDB? {
            if (INSTANCE == null) {
                synchronized(MemoDB::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        MemoDB::class.java, "memo.db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
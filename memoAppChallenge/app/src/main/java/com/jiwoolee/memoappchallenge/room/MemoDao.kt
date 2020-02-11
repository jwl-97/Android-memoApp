package com.jiwoolee.memoappchallenge.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface MemoDao {
    @Query("SELECT * FROM memo")
    fun getAll(): List<Memo>

    @Insert(onConflict = REPLACE)
    fun insert(memo: Memo)
}
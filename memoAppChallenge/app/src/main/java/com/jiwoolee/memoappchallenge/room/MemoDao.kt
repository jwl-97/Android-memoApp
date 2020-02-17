package com.jiwoolee.memoappchallenge.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update

@Dao
interface MemoDao {
    @Query("SELECT * FROM memo")
    fun getAll(): List<Memo>

    @Insert(onConflict = REPLACE)
    fun insert(memo: Memo)

    @Query("DELETE FROM memo WHERE id = :userId")
    fun deleteById(userId: Long?)

    @Update
    fun update(memo: Memo)
}
package com.jiwoolee.memoappchallenge.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memo")

class Memo(@PrimaryKey var id: Long?,
           @ColumnInfo(name = "memoTitle") var memoTitle: String?,
           @ColumnInfo(name = "memoContent") var memoContent: String,
           @ColumnInfo(name = "memoImages") var memoImages : List<String>?
){
    constructor(): this(null,"", "", null)
}

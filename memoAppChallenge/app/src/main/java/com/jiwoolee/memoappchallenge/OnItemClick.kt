package com.jiwoolee.memoappchallenge

import com.jiwoolee.memoappchallenge.room.Memo

interface OnItemClick {
    fun onClick(memoItemList : Memo)
}
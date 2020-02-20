package com.jiwoolee.memoappchallenge.adapter

import com.jiwoolee.memoappchallenge.room.Memo

interface OnItemClick {
    fun onClick(memoItemList : Memo)
}
package com.jiwoolee.memoappchallenge.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object Converters {
    @TypeConverter
    @JvmStatic
    fun fromString(value: String?): ArrayList<String> {
        val listType: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun fromArrayLisr(list: ArrayList<String?>?): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}
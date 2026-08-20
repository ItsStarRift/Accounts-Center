package com.omerplt.starlock.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromCategory(category: AppCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): AppCategory = AppCategory.valueOf(value)
}

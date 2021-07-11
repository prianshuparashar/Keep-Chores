package com.chores.keepchores

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ToDoModel (
        var title: String,
        var description: String,
        var date: Long,
        var time: Long,
        var isDone: Int = 0,
        var isExpanded: Boolean = false,
        @PrimaryKey(autoGenerate = true)
        var id: Long = 0
)
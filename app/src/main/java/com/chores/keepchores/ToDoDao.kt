package com.chores.keepchores

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ToDoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(toDoModel: ToDoModel): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTask(toDoModel: ToDoModel): Int

    @Query("SELECT * FROM ToDoModel WHERE isDone == 0")
    fun getTask(): LiveData<List<ToDoModel>>

    @Query("UPDATE ToDoModel SET isDone = 1 WHERE id=:uid")
    fun finishTask(uid: Long)

    @Query("DELETE FROM ToDoModel WHERE id=:uid")
    fun deleteTask(uid: Long)
}
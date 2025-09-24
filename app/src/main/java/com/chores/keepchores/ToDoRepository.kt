package com.chores.keepchores

import androidx.lifecycle.LiveData

class ToDoRepository(private val dao: ToDoDao) {
    val activeTasks: LiveData<List<ToDoModel>> = dao.getTask()
    suspend fun insert(task: ToDoModel) = dao.insertTask(task)
    suspend fun update(task: ToDoModel) = dao.updateTask(task)
    suspend fun finish(id: Long) = dao.finishTask(id)
    suspend fun delete(id: Long) = dao.deleteTask(id)
}

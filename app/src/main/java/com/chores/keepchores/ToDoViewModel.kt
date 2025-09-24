package com.chores.keepchores

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ToDoViewModel(private val repository: ToDoRepository) : ViewModel() {
    val tasks: LiveData<List<ToDoModel>> = repository.activeTasks

    fun insertTask(task: ToDoModel) = viewModelScope.launch(Dispatchers.IO) { repository.insert(task) }
    fun updateTask(task: ToDoModel) = viewModelScope.launch(Dispatchers.IO) { repository.update(task) }
    fun finishTask(id: Long) = viewModelScope.launch(Dispatchers.IO) { repository.finish(id) }
    fun deleteTask(id: Long) = viewModelScope.launch(Dispatchers.IO) { repository.delete(id) }
}

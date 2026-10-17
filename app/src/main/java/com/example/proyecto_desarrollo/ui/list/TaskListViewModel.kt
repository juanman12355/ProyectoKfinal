package com.example.proyecto_desarrollo.ui.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.proyecto_desarrollo.data.local.AppDatabase
import com.example.proyecto_desarrollo.data.model.Task
import com.example.proyecto_desarrollo.data.repository.SessionManager
import com.example.proyecto_desarrollo.data.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskListViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = TaskRepository(AppDatabase.getInstance(app).taskDao())
    private val userId = SessionManager(app).getUserId()

    // Flow del DAO convertido a LiveData: se actualiza solo con cada cambio en la tabla
    val tasks: LiveData<List<Task>> = repository.observeTasks(userId).asLiveData()

    // `task` ya llega con el campo `done` actualizado
    fun toggleDone(task: Task) {
        viewModelScope.launch { repository.update(task) }
    }

    fun delete(task: Task) {
        viewModelScope.launch { repository.delete(task) }
    }

    // Reinserta la tarea con su mismo id (para "Deshacer")
    fun restore(task: Task) {
        viewModelScope.launch { repository.add(task) }
    }


}
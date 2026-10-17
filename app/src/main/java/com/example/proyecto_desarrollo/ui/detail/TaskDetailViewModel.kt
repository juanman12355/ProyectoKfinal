package com.example.proyecto_desarrollo.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.proyecto_desarrollo.R
import com.example.proyecto_desarrollo.data.local.AppDatabase
import com.example.proyecto_desarrollo.data.model.Task
import com.example.proyecto_desarrollo.data.repository.DbResult
import com.example.proyecto_desarrollo.data.repository.SessionManager
import com.example.proyecto_desarrollo.data.repository.TaskRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class TaskDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = TaskRepository(AppDatabase.getInstance(app).taskDao())
    private val userId = SessionManager(app).getUserId()

    private val taskId = MutableLiveData<Long>()

    private val _error = MutableLiveData<Int?>()
    val error: LiveData<Int?> = _error

    // Se actualiza sola cuando la tarea cambia en la base de datos (p. ej. tras editarla)
    val task: LiveData<Task?> = taskId.switchMap { id ->
        repository.observeTask(id, userId)
            .catch { _error.postValue(R.string.error_load_task) }
            .asLiveData()
    }

    fun load(id: Long) {
        if (taskId.value != id) taskId.value = id
    }

    fun toggleDone(task: Task) {
        viewModelScope.launch {
            if (repository.update(task.copy(done = !task.done)) is DbResult.Failure) {
                _error.value = R.string.error_update_task
            }
        }
    }

    fun delete(task: Task) {
        viewModelScope.launch {
            if (repository.delete(task) is DbResult.Failure) {
                _error.value = R.string.error_delete_task
            }
        }
    }

    fun onErrorHandled() {
        _error.value = null
    }
}
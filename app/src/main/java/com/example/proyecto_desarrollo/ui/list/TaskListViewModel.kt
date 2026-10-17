package com.example.proyecto_desarrollo.ui.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.proyecto_desarrollo.R
import com.example.proyecto_desarrollo.data.local.AppDatabase
import com.example.proyecto_desarrollo.data.model.Task
import com.example.proyecto_desarrollo.data.repository.DbResult
import com.example.proyecto_desarrollo.data.repository.SessionManager
import com.example.proyecto_desarrollo.data.repository.TaskRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class TaskListViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = TaskRepository(AppDatabase.getInstance(app).taskDao())
    private val userId = SessionManager(app).getUserId()

    private val _error = MutableLiveData<Int?>()
    val error: LiveData<Int?> = _error

    // Si la lectura falla, avisamos y mostramos una lista vacía en vez de cerrar la app
    val tasks: LiveData<List<Task>> = repository.observeTasks(userId)
        .catch {
            _error.postValue(R.string.error_load_tasks)
            emit(emptyList())
        }
        .asLiveData()

    // `task` ya llega con el campo `done` actualizado
    fun toggleDone(task: Task) {
        viewModelScope.launch {
            if (repository.update(task) is DbResult.Failure) {
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

    // Reinserta la tarea con su mismo id (para "Deshacer")
    fun restore(task: Task) {
        viewModelScope.launch {
            if (repository.add(task) is DbResult.Failure) {
                _error.value = R.string.error_save_task
            }
        }
    }

    // La pantalla lo llama tras mostrar el mensaje, para no repetirlo al rotar
    fun onErrorHandled() {
        _error.value = null
    }
}
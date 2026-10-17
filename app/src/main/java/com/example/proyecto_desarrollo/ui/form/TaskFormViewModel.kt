package com.example.proyecto_desarrollo.ui.form

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.proyecto_desarrollo.R
import com.example.proyecto_desarrollo.data.local.AppDatabase
import com.example.proyecto_desarrollo.data.model.Task
import com.example.proyecto_desarrollo.data.repository.DbResult
import com.example.proyecto_desarrollo.data.repository.SessionManager
import com.example.proyecto_desarrollo.data.repository.TaskRepository
import com.example.proyecto_desarrollo.util.Validators
import kotlinx.coroutines.launch

data class TaskFormErrors(
    @param:StringRes val title: Int? = null,
    @param:StringRes val description: Int? = null
) {
    fun hasErrors() = title != null || description != null
}

class TaskFormViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = TaskRepository(AppDatabase.getInstance(app).taskDao())
    private val userId = SessionManager(app).getUserId()

    private var isEditMode = false
    private var editingTask: Task? = null
    private var isSaving = false

    private val _loadedTask = MutableLiveData<Task?>()
    val loadedTask: LiveData<Task?> = _loadedTask

    private val _errors = MutableLiveData(TaskFormErrors())
    val errors: LiveData<TaskFormErrors> = _errors

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> = _saved

    private val _error = MutableLiveData<Int?>()
    val error: LiveData<Int?> = _error

    // Modo edición: carga la tarea una sola vez para rellenar el formulario
    fun load(taskId: Long) {
        if (isEditMode) return
        isEditMode = true
        viewModelScope.launch {
            when (val result = repository.getTask(taskId, userId)) {
                is DbResult.Success -> {
                    editingTask = result.value
                    _loadedTask.value = result.value
                }

                is DbResult.Failure -> _error.value = R.string.error_load_task
            }
        }
    }

    fun save(title: String, description: String) {
        if (isSaving) return          // evita guardar dos veces si el usuario toca el botón rápido

        val errors = TaskFormErrors(
            title = Validators.taskTitleError(title),
            description = Validators.taskDescriptionError(description)
        )
        _errors.value = errors
        if (errors.hasErrors()) return

        // Editando, pero la tarea no cargó o ya no existe: no guardamos nada
        if (isEditMode && editingTask == null) {
            _error.value = R.string.error_task_not_found
            return
        }

        isSaving = true
        viewModelScope.launch {
            val current = editingTask
            val failed = if (current == null) {
                repository.add(
                    Task(userId = userId, title = title.trim(), description = description.trim())
                ) is DbResult.Failure
            } else {
                repository.update(
                    current.copy(title = title.trim(), description = description.trim())
                ) is DbResult.Failure
            }

            if (failed) {
                isSaving = false      // permitimos reintentar
                _error.value = R.string.error_save_task
            } else {
                _saved.value = true
            }
        }
    }

    // Revalida el título solo si ya tenía un error visible
    fun onTitleChanged(title: String) {
        val current = _errors.value ?: return
        if (current.title != null) {
            _errors.value = current.copy(title = Validators.taskTitleError(title))
        }
    }

    // Igual para la descripción
    fun onDescriptionChanged(description: String) {
        val current = _errors.value ?: return
        if (current.description != null) {
            _errors.value = current.copy(description = Validators.taskDescriptionError(description))
        }
    }

    fun onErrorHandled() {
        _error.value = null
    }
}
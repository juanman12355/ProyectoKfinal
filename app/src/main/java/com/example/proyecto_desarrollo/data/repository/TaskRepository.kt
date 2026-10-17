/**
 * Única puerta de acceso a las tareas. Los ViewModels no conocen Room: si cambiara la
 * fuente de datos (p. ej. una API remota), solo habría que modificar esta clase.
 * Las escrituras devuelven [DbResult] para que los fallos nunca tumben la app.
 */
package com.example.proyecto_desarrollo.data.repository

import com.example.proyecto_desarrollo.data.local.TaskDao
import com.example.proyecto_desarrollo.data.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TaskRepository(private val taskDao: TaskDao) {
    fun observeTasks(userId: Long): Flow<List<Task>> = taskDao.observeAll(userId)
    fun observeTask(id: Long, userId: Long): Flow<Task?> = taskDao.observeById(id, userId)

    // Lectura única (la usa el formulario para rellenar los campos al editar)
    suspend fun getTask(id: Long, userId: Long): DbResult<Task?> =
        dbCall { taskDao.observeById(id, userId).first() }

    suspend fun add(task: Task): DbResult<Long> = dbCall { taskDao.insert(task) }
    suspend fun update(task: Task): DbResult<Unit> = dbCall { taskDao.update(task) }
    suspend fun delete(task: Task): DbResult<Unit> = dbCall { taskDao.delete(task) }
}
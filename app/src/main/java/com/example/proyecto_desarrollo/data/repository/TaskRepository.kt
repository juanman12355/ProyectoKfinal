package com.example.proyecto_desarrollo.data.repository

import com.example.proyecto_desarrollo.data.local.TaskDao
import com.example.proyecto_desarrollo.data.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    fun observeTasks(userId: Long): Flow<List<Task>> = taskDao.observeAll(userId)
    fun observeTask(id: Long, userId: Long): Flow<Task?> = taskDao.observeById(id, userId)

    suspend fun add(task: Task): Long = taskDao.insert(task)
    suspend fun update(task: Task) = taskDao.update(task)
    suspend fun delete(task: Task) = taskDao.delete(task)
}
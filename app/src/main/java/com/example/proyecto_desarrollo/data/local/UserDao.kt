package com.example.proyecto_desarrollo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.proyecto_desarrollo.data.model.User

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): User?
}
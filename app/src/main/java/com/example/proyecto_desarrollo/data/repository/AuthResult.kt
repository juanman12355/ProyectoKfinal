package com.example.proyecto_desarrollo.data.repository

import com.example.proyecto_desarrollo.data.model.User

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    object InvalidCredentials : AuthResult()
    object EmailTaken : AuthResult()
    object Failure : AuthResult()
}
package com.example.proyecto_desarrollo.ui.auth

import androidx.annotation.StringRes

// Qué está pasando con la operación (login o registro)
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object LoggedIn : AuthState()
    object Registered : AuthState()
    data class Error(@param:StringRes val messageRes: Int) : AuthState()
}

// Errores por campo; null = sin error
data class FormErrors(
    @param:StringRes val name: Int? = null,
    @param:StringRes val email: Int? = null,
    @param:StringRes val password: Int? = null,
    @param:StringRes val confirm: Int? = null
) {
    fun hasErrors() = name != null || email != null || password != null || confirm != null
}
package com.example.proyecto_desarrollo.ui.auth

import androidx.annotation.StringRes

// ¿Qué está pasando con la operación? (login o registro)
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object LoggedIn : AuthState()
    object Registered : AuthState()
    data class Error(@StringRes val messageRes: Int) : AuthState()
}

// Errores por campo; null = sin error
data class FormErrors(
    @StringRes val name: Int? = null,
    @StringRes val email: Int? = null,
    @StringRes val password: Int? = null,
    @StringRes val confirm: Int? = null
) {
    fun hasErrors() = name != null || email != null || password != null || confirm != null
}
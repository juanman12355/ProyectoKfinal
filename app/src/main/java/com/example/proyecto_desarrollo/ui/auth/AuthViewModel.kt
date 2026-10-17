package com.example.proyecto_desarrollo.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.proyecto_desarrollo.R
import com.example.proyecto_desarrollo.data.local.AppDatabase
import com.example.proyecto_desarrollo.data.repository.AuthResult
import com.example.proyecto_desarrollo.data.repository.SessionManager
import com.example.proyecto_desarrollo.data.repository.UserRepository
import com.example.proyecto_desarrollo.util.Validators
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val userRepository = UserRepository(AppDatabase.getInstance(app).userDao())
    private val session = SessionManager(app)

    private val _state = MutableLiveData<AuthState>(AuthState.Idle)
    val state: LiveData<AuthState> = _state

    private val _formErrors = MutableLiveData(FormErrors())
    val formErrors: LiveData<FormErrors> = _formErrors

    fun isLoggedIn(): Boolean = session.isLoggedIn()

    fun login(email: String, password: String) {
        val errors = FormErrors(
            email = Validators.emailError(email),
            password = Validators.passwordError(password)
        )
        _formErrors.value = errors
        if (errors.hasErrors()) return

        viewModelScope.launch {
            _state.value = AuthState.Loading
            _state.value = when (val result = userRepository.login(email, password)) {
                is AuthResult.Success -> {
                    session.saveUser(result.user.id)
                    AuthState.LoggedIn
                }
                AuthResult.InvalidCredentials -> AuthState.Error(R.string.error_invalid_credentials)
                else -> AuthState.Error(R.string.error_unexpected)
            }
        }
    }

    fun register(name: String, email: String, password: String, confirm: String) {
        val errors = FormErrors(
            name = Validators.nameError(name),
            email = Validators.emailError(email),
            password = Validators.passwordError(password),
            confirm = Validators.confirmError(password, confirm)
        )
        _formErrors.value = errors
        if (errors.hasErrors()) return

        viewModelScope.launch {
            _state.value = AuthState.Loading
            _state.value = when (userRepository.register(name, email, password)) {
                is AuthResult.Success -> AuthState.Registered
                AuthResult.EmailTaken -> AuthState.Error(R.string.error_email_taken)
                else -> AuthState.Error(R.string.error_unexpected)
            }
        }
    }

    fun logout() = session.clear()

    // La pantalla lo llama tras reaccionar al estado, para no repetir la acción al rotar
    fun onStateHandled() {
        _state.value = AuthState.Idle
    }
    // Evita que un error de una pantalla aparezca "pegado" en la otra
    fun clearErrors() {
        _formErrors.value = FormErrors()
    }
}
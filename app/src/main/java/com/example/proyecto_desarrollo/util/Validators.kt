package com.example.proyecto_desarrollo.util

import android.util.Patterns
import androidx.annotation.StringRes
import com.example.proyecto_desarrollo.R

object Validators {
    private const val MIN_PASSWORD_LENGTH = 6

    // Cada función devuelve el id del mensaje de error, o null si el campo es valido
    @StringRes
    fun nameError(name: String): Int? =
        if (name.isBlank()) R.string.error_name_required else null

    @StringRes
    fun emailError(email: String): Int? = when {
        email.isBlank() -> R.string.error_email_required
        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> R.string.error_email_invalid
        else -> null
    }

    @StringRes
    fun passwordError(password: String): Int? =
        if (password.length < MIN_PASSWORD_LENGTH) R.string.error_password_short else null

    @StringRes
    fun confirmError(password: String, confirm: String): Int? =
        if (password != confirm) R.string.error_password_mismatch else null

    private const val MAX_TITLE_LENGTH = 60
    private const val MAX_DESCRIPTION_LENGTH = 300

    @StringRes
    fun taskTitleError(title: String): Int? = when {
        title.isBlank() -> R.string.error_title_required
        title.trim().length > MAX_TITLE_LENGTH -> R.string.error_title_too_long
        else -> null
    }

    @StringRes
    fun taskDescriptionError(description: String): Int? =
        if (description.trim().length > MAX_DESCRIPTION_LENGTH) R.string.error_description_too_long else null

}
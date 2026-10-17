/** Resultado de una operación de base de datos: éxito con un valor, o fallo con su causa. */
package com.example.proyecto_desarrollo.data.repository

import kotlinx.coroutines.CancellationException
import android.util.Log

// Resultado de una operación de base de datos: o salió bien, o falló con un motivo
sealed class DbResult<out T> {
    data class Success<out T>(val value: T) : DbResult<T>()
    data class Failure(val error: Throwable) : DbResult<Nothing>()
}

// Ejecuta una operación y captura sus fallos en vez de dejar que tumben la app
suspend fun <T> dbCall(block: suspend () -> T): DbResult<T> =
    try {
        DbResult.Success(block())
    } catch (e: CancellationException) {
        throw e                      // una cancelación NO es un error: debe seguir su curso
    } catch (e: Exception) {
        Log.e("DbResult", "Falló una operación de base de datos", e)
        DbResult.Failure(e)
    }
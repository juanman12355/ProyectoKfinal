package com.example.proyecto_desarrollo.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.proyecto_desarrollo.data.local.UserDao
/**
 * Reglas de acceso a datos de usuarios: registro e inicio de sesión.
 * Normaliza el correo (trim + minúsculas), guarda la contraseña como hash con salt
 * y traduce los errores técnicos de Room a un [AuthResult] que la interfaz entiende.
 */
import com.example.proyecto_desarrollo.data.model.User
import com.example.proyecto_desarrollo.util.HashUtil
import kotlinx.coroutines.CancellationException

class UserRepository(private val userDao: UserDao) {

    suspend fun register(name: String, email: String, password: String): AuthResult {
        return try {
            val salt = HashUtil.generateSalt()
            val user = User(
                name = name.trim(),
                email = email.trim().lowercase(),
                passwordHash = HashUtil.hash(password, salt),
                salt = salt
            )
            val newId = userDao.insert(user)
            AuthResult.Success(user.copy(id = newId))
        } catch (e: SQLiteConstraintException) {
            AuthResult.EmailTaken          // viola el índice único del email
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AuthResult.Failure
        }
    }

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val user = userDao.findByEmail(email.trim().lowercase())
                ?: return AuthResult.InvalidCredentials
            val hash = HashUtil.hash(password, user.salt)
            if (hash == user.passwordHash) AuthResult.Success(user)
            else AuthResult.InvalidCredentials
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AuthResult.Failure
        }
    }
}
package com.hom8.app.domain.repository

import com.hom8.app.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<Usuario?>
    val isAuthenticated: Flow<Boolean>

    suspend fun signInWithEmail(email: String, password: String): Result<Usuario>
    suspend fun signUpWithEmail(email: String, password: String, nombre: String): Result<Usuario>
    suspend fun signInWithGoogle(idToken: String): Result<Usuario>
    suspend fun signOut()
    suspend fun resetPassword(email: String): Result<Unit>
    fun getCurrentUserId(): String?
}

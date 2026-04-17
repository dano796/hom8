package com.hom8.app.domain.repository

import com.hom8.app.domain.model.Hogar
import com.hom8.app.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun getHome(homeId: String): Flow<Hogar?>
    fun getMembers(homeId: String): Flow<List<Usuario>>

    suspend fun createHome(nombre: String, creatorId: String): Result<Hogar>
    suspend fun joinHome(code: String, userId: String): Result<Hogar>
    suspend fun updateHome(hogar: Hogar): Result<Unit>
    suspend fun generateInviteCode(homeId: String): Result<String>
    suspend fun removeMember(homeId: String, userId: String): Result<Unit>
    suspend fun updateMemberRole(homeId: String, userId: String, role: String): Result<Unit>
}

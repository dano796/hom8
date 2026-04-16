package com.homeflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.homeflow.app.data.local.entity.HomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeDao {

    @Query("SELECT * FROM homes WHERE id = :homeId")
    fun getHomeById(homeId: String): Flow<HomeEntity?>

    @Query("SELECT * FROM homes WHERE codigo_invitacion = :code")
    suspend fun getHomeByInviteCode(code: String): HomeEntity?

    @Query("SELECT * FROM homes WHERE miembros LIKE :memberPattern LIMIT 1")
    suspend fun findHomeByMember(memberPattern: String): HomeEntity?

    @Query("SELECT * FROM homes WHERE miembros LIKE :memberPattern ORDER BY creado_en ASC")
    fun getHomesForUser(memberPattern: String): Flow<List<HomeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHome(home: HomeEntity)

    @Update
    suspend fun updateHome(home: HomeEntity)

    @Query("DELETE FROM homes WHERE id = :homeId")
    suspend fun deleteHome(homeId: String)
}

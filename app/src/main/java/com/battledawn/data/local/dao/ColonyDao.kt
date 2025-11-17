package com.battledawn.data.local.dao

import androidx.room.*
import com.battledawn.data.local.entity.ColonyEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Colony operations
 */
@Dao
interface ColonyDao {

    @Query("SELECT * FROM colonies WHERE id = :colonyId")
    fun getColony(colonyId: String): Flow<ColonyEntity?>

    @Query("SELECT * FROM colonies WHERE playerId = :playerId")
    fun getPlayerColonies(playerId: String): Flow<List<ColonyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColony(colony: ColonyEntity)

    @Update
    suspend fun updateColony(colony: ColonyEntity)

    @Delete
    suspend fun deleteColony(colony: ColonyEntity)

    @Query("DELETE FROM colonies WHERE id = :colonyId")
    suspend fun deleteColonyById(colonyId: String)

    @Query("SELECT * FROM colonies")
    fun getAllColonies(): Flow<List<ColonyEntity>>
}

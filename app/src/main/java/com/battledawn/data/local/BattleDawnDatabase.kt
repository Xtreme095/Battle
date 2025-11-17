package com.battledawn.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.battledawn.data.local.dao.ColonyDao
import com.battledawn.data.local.entity.ColonyEntity

/**
 * Main Room database for Battle Dawn
 */
@Database(
    entities = [
        ColonyEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BattleDawnDatabase : RoomDatabase() {

    abstract fun colonyDao(): ColonyDao

    companion object {
        const val DATABASE_NAME = "battle_dawn_db"
    }
}

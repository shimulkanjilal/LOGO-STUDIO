package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.RecentExportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentExportDao {
    @Query("SELECT * FROM recent_exports ORDER BY dateExported DESC LIMIT :limit")
    fun getRecentExports(limit: Int = 50): Flow<List<RecentExportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExport(export: RecentExportEntity): Long

    @Query("DELETE FROM recent_exports WHERE id = :id")
    suspend fun deleteExportById(id: Long)

    @Query("DELETE FROM recent_exports")
    suspend fun clearAll()
}

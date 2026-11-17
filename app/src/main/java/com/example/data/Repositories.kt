// © 2026 App Owner. All rights reserved.
package com.example.data

import com.example.data.dao.PresetDao
import com.example.data.dao.RecentExportDao
import com.example.data.entity.PresetEntity
import com.example.data.entity.RecentExportEntity
import kotlinx.coroutines.flow.Flow

class PresetRepository(private val presetDao: PresetDao) {
    val allPresets: Flow<List<PresetEntity>> = presetDao.getAllPresets()

    suspend fun insert(preset: PresetEntity): Long = presetDao.insertPreset(preset)

    suspend fun delete(id: Long) = presetDao.deletePresetById(id)
}

class RecentExportRepository(private val recentExportDao: RecentExportDao) {
    val recentExports: Flow<List<RecentExportEntity>> = recentExportDao.getRecentExports(50)

    suspend fun recordExport(export: RecentExportEntity): Long = recentExportDao.insertExport(export)

    suspend fun clearHistory() = recentExportDao.clearAll()
}

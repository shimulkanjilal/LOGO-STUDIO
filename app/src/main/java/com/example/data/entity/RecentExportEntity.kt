package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_exports")
data class RecentExportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val displayName: String,
    val mediaStoreUri: String,
    val dateExported: Long = System.currentTimeMillis(),
    val width: Int = 0,
    val height: Int = 0,
    val photoCount: Int = 1
)

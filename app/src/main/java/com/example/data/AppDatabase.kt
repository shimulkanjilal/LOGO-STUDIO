// © 2026 App Owner. All rights reserved.
package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.PresetDao
import com.example.data.dao.RecentExportDao
import com.example.data.entity.PresetEntity
import com.example.data.entity.RecentExportEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import androidx.room.migration.Migration

@Database(
    entities = [PresetEntity::class, RecentExportEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun presetDao(): PresetDao
    abstract fun recentExportDao(): RecentExportDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE logo_presets ADD COLUMN rotationDegrees REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE logo_presets ADD COLUMN flipHorizontal INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE logo_presets ADD COLUMN shadowEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE logo_presets ADD COLUMN shadowRadius REAL NOT NULL DEFAULT 10.0")
                db.execSQL("ALTER TABLE logo_presets ADD COLUMN watermarkText TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE logo_presets ADD COLUMN normalizedX REAL NOT NULL DEFAULT 0.78")
                db.execSQL("ALTER TABLE logo_presets ADD COLUMN normalizedY REAL NOT NULL DEFAULT 0.78")
                db.execSQL("ALTER TABLE logo_presets ADD COLUMN removeWhiteBg INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE logo_presets ADD COLUMN adaptiveScale INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE logo_presets ADD COLUMN textColor INTEGER NOT NULL DEFAULT -1")
                db.execSQL("ALTER TABLE logo_presets ADD COLUMN isTextBold INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE logo_presets ADD COLUMN fontSizeFraction REAL NOT NULL DEFAULT 0.035")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nubcc_studio_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default starter presets
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getInstance(context).presetDao()
                                dao.insertPreset(
                                    PresetEntity(
                                        name = "Corner Classic (Bottom Right 18%)",
                                        presetName = "BOTTOM_RIGHT",
                                        normalizedWidth = 0.18f,
                                        normalizedMargin = 0.035f,
                                        opacity = 1.0f
                                    )
                                )
                                dao.insertPreset(
                                    PresetEntity(
                                        name = "Signature (Bottom Left 18%)",
                                        presetName = "BOTTOM_LEFT",
                                        normalizedWidth = 0.18f,
                                        normalizedMargin = 0.035f,
                                        opacity = 1.0f
                                    )
                                )
                                dao.insertPreset(
                                    PresetEntity(
                                        name = "Top Crest (Top Right 15%)",
                                        presetName = "TOP_RIGHT",
                                        normalizedWidth = 0.15f,
                                        normalizedMargin = 0.03f,
                                        opacity = 0.95f
                                    )
                                )
                                dao.insertPreset(
                                    PresetEntity(
                                        name = "Subtle Center Watermark (25%)",
                                        presetName = "CENTER",
                                        normalizedWidth = 0.25f,
                                        normalizedMargin = 0.0f,
                                        opacity = 0.35f
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

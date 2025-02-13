package com.example.tugasakhir

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TingkatAnxietyEntity::class, DataSensorEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dataAccessObject(): DataAccessObject

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_1_2) // Tambahkan migrasi
                    .build()
                INSTANCE = instance
                instance
            }
        }
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. Buat tabel baru dengan struktur baru
                database.execSQL("""
            CREATE TABLE data_sensor_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                timestamp INTEGER NOT NULL,
                nn20 INTEGER NOT NULL,
                scrFreq REAL NOT NULL,
                scrRisetimeMax REAL NOT NULL,
                scrRisetimeMin REAL NOT NULL,
                scrRisetimeStd REAL NOT NULL
            )
        """)

                // 2. Salin data dari tabel lama ke tabel baru
                database.execSQL("""
            INSERT INTO data_sensor_new (id, timestamp, nn20, scrFreq, scrRisetimeMax, scrRisetimeMin, scrRisetimeStd)
            SELECT id, timestamp, nn20, scrFreq, scrAmplitudeMax, scrNumber, scrAmplitudeStd FROM data_sensor
        """)

                // 3. Hapus tabel lama
                database.execSQL("DROP TABLE data_sensor")

                // 4. Ganti nama tabel baru menjadi tabel lama
                database.execSQL("ALTER TABLE data_sensor_new RENAME TO data_sensor")
            }
        }

    }
}

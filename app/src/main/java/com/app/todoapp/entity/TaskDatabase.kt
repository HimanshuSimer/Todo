package com.app.todoapp.entity

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TaskEntity::class], version = 4, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getDatabase(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4) // Ensure all migrations
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Migration from version 1 to 2 (Adding 'category' column)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tasks ADD COLUMN category TEXT DEFAULT '' NOT NULL")
            }
        }

        // Migration from version 2 to 3 (Adding 'reminderTime' column)
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tasks ADD COLUMN reminderTime TEXT DEFAULT '' NOT NULL")
            }
        }

        // 🔥 Migration from version 3 to 4 (Ensure Correct Schema)
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Recreate the table properly with the correct schema
                database.execSQL("""
                    CREATE TABLE new_tasks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        category TEXT  NULL DEFAULT '',
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        reminderTime TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())

                // Copy old data
                database.execSQL("""
                    INSERT INTO new_tasks (id, title, description, category, isCompleted, reminderTime)
                    SELECT id, title, description, COALESCE(category, ''), isCompleted, COALESCE(reminderTime, '')
                    FROM tasks
                """.trimIndent())

                // Remove old table
                database.execSQL("DROP TABLE tasks")

                // Rename new table
                database.execSQL("ALTER TABLE new_tasks RENAME TO tasks")
            }
        }
    }
}

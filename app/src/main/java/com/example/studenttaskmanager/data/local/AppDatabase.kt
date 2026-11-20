package com.example.studenttaskmanager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.studenttaskmanager.data.model.Subject
import com.example.studenttaskmanager.data.model.Task
import com.example.studenttaskmanager.data.model.User

/**
 * One local SQLite backend for the whole app: `StudentTasks.db`
 * (documentation pack section 5). Foreign keys and indexes are declared on the
 * entities; Room enables foreign key enforcement for us.
 */
@Database(
    entities = [User::class, Subject::class, Task::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun subjectDao(): SubjectDao
    abstract fun taskDao(): TaskDao

    companion object {
        const val NAME = "StudentTasks.db"

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, NAME)
                // Schema is frozen at version 1 for the course demo. No destructive
                // fallback: a future schema change must ship a real migration.
                .build()
    }
}

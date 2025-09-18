package com.example.messengerlite.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ChatEntity::class, MessageEntity::class, OutboxMessageEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun outboxDao(): OutboxDao
}


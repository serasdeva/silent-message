package com.example.messengerlite.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY createdAt DESC")
    fun observeChats(): Flow<List<ChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ChatEntity>)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY serverSeq ASC")
    fun observeMessages(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT MAX(serverSeq) FROM messages WHERE chatId = :chatId")
    suspend fun maxSeq(chatId: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: MessageEntity)

    @Query("UPDATE messages SET deliveredAt = :ts WHERE id IN (:ids)")
    suspend fun markDelivered(ids: List<String>, ts: Long)

    @Query("UPDATE messages SET readAt = :ts WHERE id IN (:ids)")
    suspend fun markRead(ids: List<String>, ts: Long)

    @Query("SELECT id FROM messages WHERE chatId = :chatId AND readAt IS NULL AND senderId != :me LIMIT 100")
    suspend fun unreadIdsForChat(chatId: String, me: String): List<String>
}

@Dao
interface OutboxDao {
    @Query("SELECT * FROM outbox ORDER BY createdAt ASC")
    suspend fun all(): List<OutboxMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: OutboxMessageEntity)

    @Query("DELETE FROM outbox WHERE clientId = :clientId")
    suspend fun delete(clientId: String)

    @Query("UPDATE outbox SET attempts = attempts + 1 WHERE clientId = :clientId")
    suspend fun incAttempts(clientId: String)
}


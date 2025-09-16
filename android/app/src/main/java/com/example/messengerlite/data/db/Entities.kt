package com.example.messengerlite.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val createdAt: Long
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val body: String,
    val createdAt: Long,
    val serverSeq: Long,
    val deliveredAt: Long? = null,
    val readAt: Long? = null
)


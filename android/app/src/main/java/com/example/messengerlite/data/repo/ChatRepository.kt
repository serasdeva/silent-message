package com.example.messengerlite.data.repo

import com.example.messengerlite.data.db.ChatDao
import com.example.messengerlite.data.db.ChatEntity
import com.example.messengerlite.net.ApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val api: ApiService,
    private val chatDao: ChatDao
) {
    fun observeChats(): Flow<List<ChatEntity>> = chatDao.observeChats()

    suspend fun sync() {
        val res = api.listChats()
        val chats = res["chats"].orEmpty().map { dto ->
            ChatEntity(id = dto.id, createdAt = dto.createdAt)
        }
        chatDao.upsertAll(chats)
    }

    suspend fun ensureChat(otherUserId: String): String {
        val res = api.ensureChat(com.example.messengerlite.net.CreateChatWithRequest(otherUserId))
        val chatId = res["chatId"] as? String ?: ""
        sync()
        return chatId
    }
}


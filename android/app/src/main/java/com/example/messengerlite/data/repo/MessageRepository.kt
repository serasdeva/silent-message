package com.example.messengerlite.data.repo

import com.example.messengerlite.data.db.MessageDao
import com.example.messengerlite.data.db.OutboxDao
import com.example.messengerlite.data.db.OutboxMessageEntity
import com.example.messengerlite.data.db.MessageEntity
import com.example.messengerlite.net.ApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val api: ApiService,
    private val messageDao: MessageDao,
    private val outboxDao: OutboxDao
) {
    fun observeMessages(chatId: String): Flow<List<MessageEntity>> = messageDao.observeMessages(chatId)

    suspend fun sync(chatId: String) {
        val after = messageDao.maxSeq(chatId) ?: 0L
        val res = api.getMessages(chatId, if (after == 0L) null else after)
        val msgs = res["messages"].orEmpty().map { d ->
            MessageEntity(
                id = d.id,
                chatId = d.chatId,
                senderId = d.senderId,
                body = d.body,
                createdAt = d.createdAt,
                serverSeq = d.serverSeq
            )
        }
        if (msgs.isNotEmpty()) messageDao.upsertAll(msgs)
    }

    suspend fun send(chatId: String, body: String, clientId: String) {
        try {
            api.sendMessage(com.example.messengerlite.net.CreateMessageRequest(chatId, clientId, body))
        } catch (_: Throwable) {
            outboxDao.upsert(OutboxMessageEntity(clientId, chatId, body, System.currentTimeMillis()))
        }
    }
}


package com.example.messengerlite.ws

import com.example.messengerlite.BuildConfig
import com.example.messengerlite.data.db.MessageDao
import com.example.messengerlite.data.db.MessageEntity
import com.example.messengerlite.store.TokenStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject

@Singleton
class WsManager @Inject constructor(
    private val client: OkHttpClient,
    private val tokenStore: TokenStore,
    private val messageDao: MessageDao
) {
    private var ws: WebSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var onTyping: ((chatId: String, userId: String, isTyping: Boolean) -> Unit)? = null

    fun connect(onTypingCallback: ((String, String, Boolean) -> Unit)? = null) {
        onTyping = onTypingCallback
        val token = tokenStore.accessToken() ?: return
        val req = Request.Builder()
            .url("${BuildConfig.WS_URL}/ws?token=$token")
            .build()
        ws = client.newWebSocket(req, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                runCatching {
                    val obj = JSONObject(text)
                    if (obj.optString("type") == "message:new") {
                        val m = obj.getJSONObject("message")
                        val entity = MessageEntity(
                            id = m.getString("id"),
                            chatId = m.getString("chatId"),
                            senderId = m.getString("senderId"),
                            body = m.getString("body"),
                            createdAt = m.getLong("createdAt"),
                            serverSeq = m.getLong("serverSeq")
                        )
                        scope.launch { messageDao.upsert(entity) }
                    }
                    if (obj.optString("type") == "receipt:update") {
                        val id = obj.getString("messageId")
                        val delivered = if (obj.has("deliveredAt")) obj.getLong("deliveredAt") else null
                        val read = if (obj.has("readAt")) obj.getLong("readAt") else null
                        scope.launch {
                            delivered?.let { messageDao.markDelivered(listOf(id), it) }
                            read?.let { messageDao.markRead(listOf(id), it) }
                        }
                    }
                    if (obj.optString("type") == "typing") {
                        val chatId = obj.getString("chatId")
                        val userId = obj.getString("userId")
                        val isTyping = obj.getBoolean("isTyping")
                        onTyping?.invoke(chatId, userId, isTyping)
                    }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) { }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) { }
        })
    }

    fun sendTyping(chatId: String, isTyping: Boolean) {
        val payload = JSONObject(mapOf("type" to "typing", "chatId" to chatId, "isTyping" to isTyping)).toString()
        ws?.send(payload)
    }

    fun sendReceiptDelivered(id: String) {
        val payload = JSONObject(mapOf("type" to "receipt", "receiptType" to "delivered", "messageIds" to listOf(id))).toString()
        ws?.send(payload)
    }

    fun sendReceiptRead(ids: List<String>) {
        val payload = JSONObject(mapOf("type" to "receipt", "receiptType" to "read", "messageIds" to ids)).toString()
        ws?.send(payload)
    }
}


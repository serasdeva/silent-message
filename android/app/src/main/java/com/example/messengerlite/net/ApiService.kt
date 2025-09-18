package com.example.messengerlite.net

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class OtpRequest(val phone: String)
data class OtpVerifyRequest(val requestId: String, val code: String)
data class TokenPair(val accessToken: String, val refreshToken: String)
data class UserDto(val id: String, val phone: String?, val displayName: String?)
data class OtpVerifyResponse(val accessToken: String, val refreshToken: String, val user: UserDto)

data class ChatDto(val id: String, val members: List<String>, val createdAt: Long)
data class MessageDto(val id: String, val chatId: String, val senderId: String, val body: String, val createdAt: Long, val serverSeq: Long)

data class CreateChatWithRequest(val userId: String)
data class CreateMessageRequest(val chatId: String, val clientId: String?, val body: String)
data class ReceiptsRequest(val messageIds: List<String>, val type: String)

interface ApiService {
    @POST("/auth/otp/request")
    suspend fun requestOtp(@Body body: OtpRequest): Map<String, String>

    @POST("/auth/otp/verify")
    suspend fun verifyOtp(@Body body: OtpVerifyRequest): OtpVerifyResponse

    @GET("/chats")
    suspend fun listChats(): Map<String, List<ChatDto>>

    @POST("/chats/with")
    suspend fun ensureChat(@Body body: CreateChatWithRequest): Map<String, String>

    @GET("/chats/{id}/messages")
    suspend fun getMessages(@Path("id") chatId: String, @Query("after") after: Long? = null): Map<String, List<MessageDto>>

    @POST("/messages")
    suspend fun sendMessage(@Body body: CreateMessageRequest): Map<String, Any>

    @POST("/messages/receipts")
    suspend fun sendReceipts(@Body body: ReceiptsRequest): Map<String, Any>

    @POST("/devices")
    suspend fun registerDevice(@Body body: Map<String, String>): Map<String, Any>
}


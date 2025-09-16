package com.example.messengerlite.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.messengerlite.data.db.AppDatabase
import com.example.messengerlite.net.ApiService
import com.example.messengerlite.net.CreateMessageRequest
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class OutboxWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            val db = androidx.room.Room.databaseBuilder(applicationContext, AppDatabase::class.java, "messenger.db").build()
            val outbox = db.outboxDao().all()
            val tokenStore = com.example.messengerlite.store.TokenStore(applicationContext)
            val auth = Interceptor { chain ->
                val at = tokenStore.accessToken()
                val req = if (!at.isNullOrEmpty()) chain.request().newBuilder().addHeader("Authorization", "Bearer $at").build() else chain.request()
                chain.proceed(req)
            }
            val retrofit = Retrofit.Builder()
                .baseUrl(com.example.messengerlite.BuildConfig.BASE_URL)
                .client(OkHttpClient.Builder().addInterceptor(auth).build())
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
            val api = retrofit.create(ApiService::class.java)
            for (m in outbox) {
                try {
                    api.sendMessage(CreateMessageRequest(m.chatId, m.clientId, m.body))
                    db.outboxDao().delete(m.clientId)
                } catch (_: Throwable) {
                    db.outboxDao().incAttempts(m.clientId)
                }
            }
            Result.success()
        } catch (_: Throwable) {
            Result.retry()
        }
    }
}


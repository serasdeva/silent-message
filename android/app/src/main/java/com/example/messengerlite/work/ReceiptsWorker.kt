package com.example.messengerlite.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.messengerlite.net.ApiService
import com.example.messengerlite.net.ReceiptsRequest
import com.example.messengerlite.store.TokenStore
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ReceiptsWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val ids = inputData.getStringArray(KEY_IDS)?.toList().orEmpty()
        val type = inputData.getString(KEY_TYPE) ?: return Result.failure()
        if (ids.isEmpty()) return Result.success()
        return try {
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
            api.sendReceipts(ReceiptsRequest(ids, type))
            Result.success()
        } catch (_: Throwable) {
            Result.retry()
        }
    }

    companion object {
        const val KEY_IDS = "ids"
        const val KEY_TYPE = "type"
    }
}


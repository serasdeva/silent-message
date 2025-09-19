package com.example.messengerlite.di

import android.content.Context
import androidx.room.Room
import com.example.messengerlite.data.db.AppDatabase
import com.example.messengerlite.data.db.ChatDao
import com.example.messengerlite.data.db.OutboxDao
import com.example.messengerlite.data.db.MessageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "messenger.db").fallbackToDestructiveMigration().build()

    @Provides fun chatDao(db: AppDatabase): ChatDao = db.chatDao()
    @Provides fun messageDao(db: AppDatabase): MessageDao = db.messageDao()
    @Provides fun outboxDao(db: AppDatabase): OutboxDao = db.outboxDao()
}


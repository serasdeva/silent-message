package com.example.messengerlite

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.messengerlite.data.db.AppDatabase
import com.example.messengerlite.data.db.ChatEntity
import com.example.messengerlite.data.db.MessageEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DbDaoTest {
    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java).build()
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun insertAndRead() = runBlocking {
        db.chatDao().upsertAll(listOf(ChatEntity("chat-1", 1L)))
        db.messageDao().upsert(MessageEntity("m1", "chat-1", "user_a", "hi", 1L, 1L))
        val maxSeq = db.messageDao().maxSeq("chat-1")
        assertEquals(1L, maxSeq)
    }
}


package com.example.messengerlite.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messengerlite.data.db.ChatEntity
import com.example.messengerlite.data.repo.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@HiltViewModel
class ChatListViewModel @Inject constructor(private val repo: ChatRepository) : ViewModel() {
    val chats: Flow<List<ChatEntity>> = repo.observeChats()
    fun refresh() { viewModelScope.launch { runCatching { repo.sync() } } }
    fun ensureChat(otherUserId: String) { viewModelScope.launch { runCatching { repo.ensureChat(otherUserId) } } }
}


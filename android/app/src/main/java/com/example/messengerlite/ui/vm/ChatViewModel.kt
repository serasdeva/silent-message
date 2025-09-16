package com.example.messengerlite.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messengerlite.data.db.MessageEntity
import com.example.messengerlite.data.repo.MessageRepository
import com.example.messengerlite.ws.WsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: MessageRepository,
    private val ws: WsManager
) : ViewModel() {
    private val chatIdState = MutableStateFlow<String?>(null)

    val messages: Flow<List<MessageEntity>> = chatIdState.flatMapLatest { id ->
        if (id == null) kotlinx.coroutines.flow.flowOf(emptyList()) else repo.observeMessages(id)
    }

    fun setChat(chatId: String) {
        chatIdState.value = chatId
        viewModelScope.launch { repo.sync(chatId) }
        ws.connect()
    }
}


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
import kotlinx.coroutines.flow.MutableStateFlow as MutableState

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: MessageRepository,
    private val ws: WsManager
) : ViewModel() {
    private val chatIdState = MutableStateFlow<String?>(null)
    val typing = MutableState(false)

    val messages: Flow<List<MessageEntity>> = chatIdState.flatMapLatest { id ->
        if (id == null) kotlinx.coroutines.flow.flowOf(emptyList()) else repo.observeMessages(id)
    }

    fun setChat(chatId: String) {
        chatIdState.value = chatId
        viewModelScope.launch { repo.sync(chatId) }
        ws.connect { cid, _, isTyping -> if (cid == chatId) typing.value = isTyping }
    }

    fun send(body: String) {
        val id = chatIdState.value ?: return
        val clientId = java.util.UUID.randomUUID().toString()
        viewModelScope.launch { repo.send(id, body, clientId) }
    }

    fun typing(isTyping: Boolean) {
        val id = chatIdState.value ?: return
        ws.sendTyping(id, isTyping)
    }

    fun markRead(ids: List<String>) {
        ws.sendReceiptRead(ids)
    }
}


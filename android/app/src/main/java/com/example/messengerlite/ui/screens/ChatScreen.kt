package com.example.messengerlite.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.messengerlite.ui.vm.ChatViewModel

@Composable
fun ChatScreen(chatId: String, vm: ChatViewModel = hiltViewModel()) {
    LaunchedEffect(chatId) { vm.setChat(chatId) }
    val msgs = vm.messages.collectAsState(emptyList())
    Column(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize()) {
            items(msgs.value) { m ->
                ListItem(headlineContent = { Text(m.body) }, supportingContent = { Text(m.senderId.takeLast(4)) })
            }
        }
    }
}


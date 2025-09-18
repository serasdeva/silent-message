package com.example.messengerlite.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.messengerlite.ui.vm.ChatViewModel

@Composable
fun ChatScreen(chatId: String, vm: ChatViewModel = hiltViewModel()) {
    LaunchedEffect(chatId) { vm.setChat(chatId) }
    val msgs = vm.messages.collectAsState(emptyList())
    var text by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(8.dp)) {
        if (vm.typing.value) {
            Text("печатает…", modifier = Modifier.padding(4.dp))
        }
        LazyColumn(Modifier.weight(1f)) {
            items(msgs.value) { m ->
                val status = when {
                    m.readAt != null -> "read"
                    m.deliveredAt != null -> "delivered"
                    else -> "sent"
                }
                ListItem(headlineContent = { Text(m.body) }, supportingContent = { Text("${m.senderId.takeLast(4)} • $status") })
            }
        }
        Row {
            OutlinedTextField(value = text, onValueChange = { text = it; vm.typing(true) }, modifier = Modifier.weight(1f))
            Button(onClick = { if (text.isNotBlank()) { vm.send(text); text = "" } }) { Text("Send") }
        }
    }
}


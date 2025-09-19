package com.example.messengerlite.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.messengerlite.ui.vm.ChatListViewModel
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable

@Composable
fun ChatListScreen(vm: ChatListViewModel = hiltViewModel(), onOpenChat: (String) -> Unit = {}) {
    val chats = vm.chats.collectAsState(emptyList())
    LaunchedEffect(Unit) { vm.refresh() }
    var otherUser by remember { mutableStateOf("user_b") }
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Row(Modifier.padding(12.dp)) {
                OutlinedTextField(value = otherUser, onValueChange = { otherUser = it }, label = { Text("User ID") }, modifier = Modifier.weight(1f))
                Button(onClick = { vm.ensureChat(otherUser) }) { Text("Create 1:1") }
            }
        }
        items(chats.value) { c ->
            ListItem(
                headlineContent = { Text("Chat ${c.id.takeLast(4)}") },
                supportingContent = { Text("created ${c.createdAt}") },
                modifier = Modifier.padding(horizontal = 8.dp).clickable { onOpenChat(c.id) },
                overlineContent = { },
                trailingContent = { },
                leadingContent = { }
            )
        }
    }
}


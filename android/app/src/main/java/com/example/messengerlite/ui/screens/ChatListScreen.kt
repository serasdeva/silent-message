package com.example.messengerlite.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun ChatListScreen() {
    val items = remember { mutableStateListOf("Chat with user_b", "Chat with user_c") }
    LazyColumn(Modifier.fillMaxSize()) {
        items(items) { title ->
            ListItem(headlineContent = { Text(title) })
        }
    }
}


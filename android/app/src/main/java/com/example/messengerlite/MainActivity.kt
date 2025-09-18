package com.example.messengerlite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.messengerlite.ui.navigation.AppNavHost
import com.example.messengerlite.ui.navigation.Routes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val nav = rememberNavController()
            Surface(color = MaterialTheme.colorScheme.background) {
                val data = intent?.data
                if (data != null && data.host == "chat") {
                    val chatId = data.getQueryParameter("chatId")
                    if (!chatId.isNullOrBlank()) {
                        nav.navigate("chat/$chatId")
                    }
                }
                AppNavHost(nav)
            }
        }
    }
}


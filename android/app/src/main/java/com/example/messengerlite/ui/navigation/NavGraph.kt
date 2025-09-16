package com.example.messengerlite.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.messengerlite.ui.screens.ChatListScreen
import com.example.messengerlite.ui.screens.LoginScreen

object Routes {
    const val Login = "login"
    const val ChatList = "chats"
}

@Composable
fun AppNavHost(nav: NavHostController) {
    NavHost(navController = nav, startDestination = Routes.Login) {
        composable(Routes.Login) {
            val vm = hiltViewModel<com.example.messengerlite.ui.vm.AuthViewModel>()
            LoginScreen(vm) { nav.navigate(Routes.ChatList) { popUpTo(Routes.Login) { inclusive = true } } }
        }
        composable(Routes.ChatList) {
            ChatListScreen()
        }
    }
}


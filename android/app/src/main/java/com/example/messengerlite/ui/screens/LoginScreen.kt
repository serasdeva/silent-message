package com.example.messengerlite.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.messengerlite.ui.vm.AuthViewModel

@Composable
fun LoginScreen(vm: AuthViewModel, onSuccess: () -> Unit) {
    var phone by remember { mutableStateOf("+15551234567") }
    var code by remember { mutableStateOf("0000") }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Login (OTP stub)")
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
        Spacer(Modifier.height(8.dp))
        Button(onClick = { vm.requestOtp(phone) }) { Text("Request OTP") }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Code") })
        Spacer(Modifier.height(8.dp))
        Button(onClick = { vm.verify(phone, code, onSuccess) }) { Text("Verify") }
    }
}


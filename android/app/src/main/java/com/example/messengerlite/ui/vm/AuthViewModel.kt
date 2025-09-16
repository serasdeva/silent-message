package com.example.messengerlite.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messengerlite.net.ApiService
import com.example.messengerlite.net.OtpRequest
import com.example.messengerlite.net.OtpVerifyRequest
import com.example.messengerlite.store.TokenStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore
) : ViewModel() {

    fun requestOtp(phone: String) {
        viewModelScope.launch {
            runCatching { api.requestOtp(OtpRequest(phone)) }
        }
    }

    fun verify(phone: String, code: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = runCatching { api.verifyOtp(OtpVerifyRequest(requestId = "stub", code = code)) }.getOrNull()
            if (res != null) {
                tokenStore.setTokens(res.accessToken, res.refreshToken)
                onSuccess()
            }
        }
    }
}


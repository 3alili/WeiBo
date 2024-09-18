package com.sunnyweather.weibo_zhoujiaqian.data.viewmodel

import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunnyweather.weibo_zhoujiaqian.R
import com.sunnyweather.weibo_zhoujiaqian.data.model.*
import com.sunnyweather.weibo_zhoujiaqian.data.repository.Repository
import kotlinx.coroutines.launch
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

class LoginViewModel(private val repository: Repository) : ViewModel() {
    var canRequestVerifyCode = true
    private var verifyCodeRequestCount = 0
    private val maxVerifyCodeRequests = 20

    private val _loginResponse = MutableLiveData<Response<LoginResponse>>()
    val loginResponse: LiveData<Response<LoginResponse>> = _loginResponse

    private val _sendCodeResponse = MutableLiveData<Response<SendCodeResponse>>()
    val sendCodeResponse: LiveData<Response<SendCodeResponse>> = _sendCodeResponse

    private val _userInfoResponse = MutableLiveData<Response<VerifyResponse>>()
    val userInfoResponse: LiveData<Response<VerifyResponse>> = _userInfoResponse

    private val _homePageResponse = MutableLiveData<Response<HomePageResponse>>()
    val homePageResponse: LiveData<Response<HomePageResponse>> = _homePageResponse

    fun sendVerificationCode(phone: String, onResult: (Response<SendCodeResponse>) -> Unit, onError: (String) -> Unit) {
        if (canRequestVerifyCode && verifyCodeRequestCount < maxVerifyCodeRequests) {
            viewModelScope.launch {
                verifyCodeRequestCount++
                val response = repository.sendVerificationCode(phone)
                if (response.isSuccessful) {
                    _sendCodeResponse.postValue(response)
                    onResult(response)
                } else {
                    onError("Error: ${response.message()}")
                }
            }
        } else if (verifyCodeRequestCount >= maxVerifyCodeRequests) {
            onResult(Response.error(429, "验证码请求次数已达上限".toResponseBody(null)))
        }
    }

    fun login(phone: String, smsCode: String, onResult: (Response<LoginResponse>) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val response = repository.login(phone, smsCode)
            if (response.isSuccessful) {
                _loginResponse.postValue(response)
                onResult(response)
            } else {
                onError("Error: ${response.message()}")
            }
        }
    }

    fun getUserInfo(token: String, onResult: (Response<VerifyResponse>) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val response = repository.getUserInfo(token)
            if (response.isSuccessful) {
                _userInfoResponse.postValue(response)
                onResult(response)
            } else {
                onError("Error: ${response.message()}")
            }
        }
    }

    fun getHomePage(token: String, onResult: (Response<HomePageResponse>) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val response = repository.getHomePage(token)
            if (response.isSuccessful) {
                _homePageResponse.postValue(response)
                onResult(response)
            } else {
                onError("Error: ${response.message()}")
            }
        }
    }

    fun resetDailyLimit() {
        verifyCodeRequestCount = 0
    }
}

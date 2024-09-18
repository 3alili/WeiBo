package com.sunnyweather.weibo_zhoujiaqian.data.repository

import com.sunnyweather.weibo_zhoujiaqian.api.AuthApiService
import com.sunnyweather.weibo_zhoujiaqian.data.model.HomePageResponse
import com.sunnyweather.weibo_zhoujiaqian.data.model.LoginRequest
import com.sunnyweather.weibo_zhoujiaqian.data.model.LoginResponse
import com.sunnyweather.weibo_zhoujiaqian.data.model.SendCodeRequest
import com.sunnyweather.weibo_zhoujiaqian.data.model.SendCodeResponse
import com.sunnyweather.weibo_zhoujiaqian.data.model.VerifyResponse
import retrofit2.Response

class Repository(private val apiService: AuthApiService) {
    suspend fun sendVerificationCode(phone: String): Response<SendCodeResponse> {
        return apiService.sendVerificationCode(SendCodeRequest(phone))
    }

    suspend fun login(phone: String, smsCode: String): Response<LoginResponse> {
        return apiService.login(LoginRequest(phone, smsCode))
    }

    suspend fun getUserInfo(token: String): Response<VerifyResponse>{
        return apiService.getUserInfo(token)
    }

    suspend fun getHomePage(token: String): Response<HomePageResponse> {
        return apiService.getHomePage(token)
    }
}
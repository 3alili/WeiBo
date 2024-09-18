package com.sunnyweather.weibo_zhoujiaqian.api

import com.sunnyweather.weibo_zhoujiaqian.data.model.HomePageResponse
import com.sunnyweather.weibo_zhoujiaqian.data.model.VerifyResponse
import com.sunnyweather.weibo_zhoujiaqian.data.model.LoginRequest
import com.sunnyweather.weibo_zhoujiaqian.data.model.LoginResponse
import com.sunnyweather.weibo_zhoujiaqian.data.model.SendCodeRequest
import com.sunnyweather.weibo_zhoujiaqian.data.model.SendCodeResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface AuthApiService {
    @POST("/weibo/api/auth/sendCode")
    suspend fun sendVerificationCode(@Body request: SendCodeRequest): Response<SendCodeResponse>

    @POST("/weibo/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("/weibo/api/user/info")
    suspend fun getUserInfo(@Header("Authorization") token: String): Response<VerifyResponse>

    // 新增微博首页接口
    @GET("/weibo/homePage")
    suspend fun getHomePage(@Header("Authorization") token: String): Response<HomePageResponse>
}



package com.sunnyweather.weibo_zhoujiaqian.data.model

data class LoginRequest(
    val phone: String,
    val smsCode: String
)

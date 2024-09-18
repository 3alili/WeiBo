package com.sunnyweather.weibo_zhoujiaqian.data.model

data class VerifyResponse(
    val code: Int,
    val msg: String,
    val data: UserInfo
)
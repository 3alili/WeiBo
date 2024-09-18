package com.sunnyweather.weibo_zhoujiaqian.data.model

data class LoginResponse(
    val code: Int,
    val msg: String,
    val data: String // 根据实际情况确定数据类型
)

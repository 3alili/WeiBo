package com.sunnyweather.weibo_zhoujiaqian.data.model

data class SendCodeResponse(
    val code: Int,
    val msg: String,
    val data: Boolean
)

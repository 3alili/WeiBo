package com.sunnyweather.weibo_zhoujiaqian.data.model

data class UserInfo(
    val id: Long,
    val username: String,
    val phone: String,
    val avatar: String,
    val loginStatus: Boolean
)
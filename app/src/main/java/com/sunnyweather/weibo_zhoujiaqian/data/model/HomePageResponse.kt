package com.sunnyweather.weibo_zhoujiaqian.data.model

data class HomePageResponse(
    val code: Int,
    val msg: String,
    val data: HomePageData
)

data class HomePageData(
    val records: List<WeiboRecord>,
    val total: Int,
    val size: Int,
    val current: Int,
    val pages: Int
)

data class WeiboRecord(
    val id: Int,
    val userId: Int,
    val username: String,
    val phone: String,
    val avatar: String,
    val title: String,
    val videoUrl: String?,
    val poster: String?,
    val images: List<String>?,
    var likeCount: Int,
    var likeFlag: Boolean,
    val createTime: String
)

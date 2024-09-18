package com.sunnyweather.weibo_zhoujiaqian

import android.app.Application

class App : Application() {
    // 可以在这里添加全局变量或方法
    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        // 在 onCreate 方法中初始化 instance 属性
        instance = this
    }
}

package com.sunnyweather.weibo_zhoujiaqian.data.network

import android.content.Intent
import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.sunnyweather.weibo_zhoujiaqian.api.AuthApiService
import com.sunnyweather.weibo_zhoujiaqian.App
import com.sunnyweather.weibo_zhoujiaqian.data.ui.LoginActivity


import java.io.IOException

object ApiClient {
    private const val BASE_URL = "https://hotfix-service-prod.g.mi.com"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(TokenInterceptor())
        .addInterceptor { chain ->
            val response = chain.proceed(chain.request())
            if (response.code == 403) {
                // Handle 403 error
                handleTokenInvalid()
            }
            response
        }
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    //处理过期token
    private fun handleTokenInvalid() {
        clearToken()
        redirectToLogin()
    }

    private fun clearToken() {
        val sharedPreferences = App.instance.getSharedPreferences("AppPrefs", 0)
        sharedPreferences.edit().remove("token").apply()
        sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
        Log.d("ApiClient", "Token cleared and login status set to false")
    }

    //重定向到登录页面
    private fun redirectToLogin() {
        val intent = Intent(App.instance, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        App.instance.startActivity(intent)
        Log.d("ApiClient", "Redirected to LoginActivity")
    }

    class TokenInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val requestBuilder: Request.Builder = chain.request().newBuilder()
            val token = App.instance.getSharedPreferences("AppPrefs", 0).getString("token", null)
            token?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
                Log.d("TokenInterceptor", "Authorization header added")
            }
            return chain.proceed(requestBuilder.build())
        }
    }

    //生成了 AuthApiService 接口的具体实现，从而使得你可以在应用中直接调用接口方法来发起网络请求，而无需手动处理 HTTP 请求逻辑
    val authService: AuthApiService = retrofit.create(AuthApiService::class.java)
}


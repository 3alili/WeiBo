package com.sunnyweather.weibo_zhoujiaqian.data.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.weibo_zhoujiaqian.api.AuthApiService
import com.sunnyweather.weibo_zhoujiaqian.R
import com.sunnyweather.weibo_zhoujiaqian.data.model.UserInfoEvent
import com.sunnyweather.weibo_zhoujiaqian.data.network.ApiClient
import com.sunnyweather.weibo_zhoujiaqian.data.repository.Repository
import com.sunnyweather.weibo_zhoujiaqian.data.viewmodel.LoginViewModel
import com.sunnyweather.weibo_zhoujiaqian.data.viewmodel.LoginViewModelFactory
import de.greenrobot.event.EventBus
import java.util.Timer
import java.util.TimerTask

class LoginActivity : AppCompatActivity() {

    private lateinit var editPhoneNumber: EditText
    private lateinit var buttonVerify: Button
    private lateinit var buttonBack: TextView
    private lateinit var buttonLogin: Button
    private lateinit var smsCode: String
    private lateinit var phone: String
    private lateinit var token: String
    private lateinit var editSmsCode: EditText


    private val authService: AuthApiService by lazy { ApiClient.authService }
    private val repository: Repository by lazy { Repository(authService) }
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 初始化 ViewModel
        val viewModelFactory = LoginViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(LoginViewModel::class.java)



        editPhoneNumber = findViewById(R.id.edit1)
        buttonVerify = findViewById(R.id.button_verify)
        buttonBack = findViewById(R.id.back)
        buttonLogin = findViewById(R.id.button_login)
        editSmsCode = findViewById(R.id.edit2)

        buttonBack.setOnClickListener {
            finish()
        }

        // 设置监听器以监控文本变化
        editPhoneNumber.addTextChangedListener(textWatcher)

        buttonVerify.setOnClickListener {
            val phoneNumber = editPhoneNumber.text.toString()
            if (phoneNumber.length == 11) {
                startVerifyCodeRequest(phoneNumber)
            } else {
                Toast.makeText(this, "请输入完整手机号", Toast.LENGTH_SHORT).show()
            }
        }

        buttonLogin.setOnClickListener {
            phone = editPhoneNumber.text.toString()
            smsCode = editSmsCode.text.toString()
            StartLogin(phone, smsCode)
        }

        //午夜刷新验证码获取次数
        resetDailyLimitAtMidnight()

        // 自动登录
        autoLogin()
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            updateButtonStates()
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    @SuppressLint("ResourceAsColor")
    private fun updateButtonStates() {
        buttonVerify.isEnabled = viewModel.canRequestVerifyCode
        buttonVerify.setTextColor(
            if (buttonVerify.isEnabled) resources.getColor(R.color.blue)
            else resources.getColor(R.color.gray))
    }

    private fun startVerifyCodeRequest(phoneNumber: String) {
        viewModel.sendVerificationCode(phoneNumber, { response ->
            viewModel.canRequestVerifyCode = false
            updateButtonStates()
            if (response.isSuccessful && response.body()?.code == 200) {
                phone = phoneNumber
                Toast.makeText(this, "验证码已发送", Toast.LENGTH_SHORT).show()
                startCountDownTimer()
            } else {
                Toast.makeText(this, "验证码发送失败: ${response.body()?.msg}", Toast.LENGTH_SHORT).show()
                viewModel.canRequestVerifyCode = true
                updateButtonStates()
            }
        }, { errorMessage ->
            Toast.makeText(this, "请求失败: $errorMessage", Toast.LENGTH_SHORT).show()
            viewModel.canRequestVerifyCode = true
            updateButtonStates()
        })
    }

    private fun StartLogin(phone: String,smsCode: String){
        viewModel.login(phone, smsCode, { response ->
            if (response.isSuccessful && response.body()?.code == 200) {
                val token = response.body()!!.data // 获取登录返回的 token
                saveToken(token)
                getUserInfo(token){ success ->
                    if (success) {
                        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
                        Log.d("LoginActivity", "StartLogin: 被调用了")

                        finish()
                    } else {
                        Toast.makeText(this, "获取用户信息失败", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "登录失败: ${response.body()?.msg}", Toast.LENGTH_SHORT).show()
                viewModel.canRequestVerifyCode = true
            }
        }, { errorMessage ->
            Toast.makeText(this, "请求失败: $errorMessage", Toast.LENGTH_SHORT).show()
        })
    }

    private fun getUserInfo(token: String,callback: (Boolean)-> Unit) {
        val beartoken = "Bearer $token"
        viewModel.getUserInfo(beartoken, { response ->
            if (response.isSuccessful && response.body()?.code == 200) {
                Log.d("LoginActivity", "token=$token")
                val userInfo = response.body()?.data
                Log.d("LoginActivity", "userInfo = $userInfo")
                if (userInfo != null) {
                    EventBus.getDefault().post(UserInfoEvent(userInfo))
                    saveLoginStatus(userInfo.loginStatus)
                    callback(true)
                } else {
                    callback(false)
                }
            } else {
                Toast.makeText(this, "获取用户信息失败: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
            }
        }, { errorMessage ->
            Toast.makeText(this, "请求失败: $errorMessage", Toast.LENGTH_SHORT).show()
        })
    }

    private fun startCountDownTimer() {
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                buttonVerify.text = "获取验证码(${millisUntilFinished / 1000}s)"
            }

            override fun onFinish() {
                viewModel.canRequestVerifyCode = true
                buttonVerify.text = "获取验证码"
                updateButtonStates()
            }
        }.start()
    }

    private fun resetDailyLimitAtMidnight() {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        var nextMidnight = calendar.timeInMillis + 24 * 60 * 60 * 1000

        if (now > nextMidnight) {
            nextMidnight += 24 * 60 * 60 * 1000
        }

        val delay = nextMidnight - now

        Timer().schedule(object : TimerTask() {
            override fun run() {
                viewModel.resetDailyLimit()
                resetDailyLimitAtMidnight()
            }
        }, delay)
    }

    private fun autoLogin() {
        val token = getToken()
        if (token != null) {
            getUserInfo(token){ success ->
                if (success) {
                    Toast.makeText(this, "自动登录成功", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "自动登录失败，重新登录", Toast.LENGTH_SHORT).show()
                    // 这里你可以选择留在当前活动或者跳转到登录页面
                }
            }
        }
    }

    private fun getToken(): String? {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("token", null)
    }

    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("token", token).apply()
    }

    private fun saveLoginStatus(isLoggedIn: Boolean) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean("login_status", isLoggedIn)
            apply()
        }
    }
}
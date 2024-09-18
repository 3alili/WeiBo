package com.sunnyweather.weibo_zhoujiaqian.data.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.sunnyweather.weibo_zhoujiaqian.R

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("com.example.myapp.PREFERENCE_FILE_KEY", MODE_PRIVATE)

        if (isFirstLaunch()) {
            showPrivacyDialog()
        } else {
            delayLaunchHomePage()
        }
    }

    private fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean("isFirstLaunch", true)
    }

    private fun showPrivacyDialog() {
        findViewById<View>(R.id.gray_overlay).visibility = View.VISIBLE
        findViewById<View>(R.id.dialog_container).visibility = View.VISIBLE

        val privacyContent: TextView = findViewById(R.id.textView_privacy_content)
        val agreeButton: Button = findViewById(R.id.button_agree)
        val disagreeButton: Button = findViewById(R.id.button_disagree)
        // 设置 SpannableString 以处理点击事件和颜色
        val contentText = privacyContent.text.toString()
        val spannableString = SpannableString(contentText)

        val userAgreementClick = object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(this@MainActivity, "查看用户协议", Toast.LENGTH_SHORT).show()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false // 取消下划线
            }
        }

        val privacyPolicyClick = object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(this@MainActivity, "查看隐私协议", Toast.LENGTH_SHORT).show()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false // 取消下划线
            }
        }

        val userAgreementStart = contentText.indexOf("《用户协议》")
        val userAgreementEnd = userAgreementStart + "《用户协议》".length
        val privacyPolicyStart = contentText.indexOf("《隐私政策》")
        val privacyPolicyEnd = privacyPolicyStart + "《隐私政策》".length

        spannableString.setSpan(userAgreementClick, userAgreementStart, userAgreementEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(privacyPolicyClick, privacyPolicyStart, privacyPolicyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.blue)), userAgreementStart, userAgreementEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.blue)), privacyPolicyStart, privacyPolicyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        privacyContent.text = spannableString
        privacyContent.movementMethod = LinkMovementMethod.getInstance()

        agreeButton.setOnClickListener {
            sharedPreferences.edit().putBoolean("isFirstLaunch", false).apply()
            launchHomePage()
        }

        disagreeButton.setOnClickListener {
            finish()
        }

    }

    private fun delayLaunchHomePage() {
        Handler(Looper.getMainLooper()).postDelayed({
            launchHomePage()
        }, 1000) // 延时1秒 (1000毫秒)
    }

    private fun launchHomePage() {
        val intent = Intent(this, HomePageActivity::class.java)
        startActivity(intent)
        finish()
    }
}

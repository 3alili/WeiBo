package com.sunnyweather.weibo_zhoujiaqian.data.ui

import com.example.imageviewer.ImagePagerAdapter
import com.sunnyweather.weibo_zhoujiaqian.R
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class PicActivity : Activity() {
    private lateinit var viewPager: ViewPager
    private lateinit var pageIndicator: TextView
    private lateinit var userAvatar: ImageView
    private lateinit var userNickname: TextView
    private lateinit var downloadButton: TextView

    private lateinit var images: List<String>
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 隐藏状态栏和导航栏
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        actionBar?.hide()

        setContentView(R.layout.activity_pic)

        viewPager = findViewById(R.id.view_pager)
        pageIndicator = findViewById(R.id.page_indicator)
        userAvatar = findViewById(R.id.user_avatar)
        userNickname = findViewById(R.id.user_nickname)
        downloadButton = findViewById(R.id.download_button)

        // 从Intent中获取用户信息和图片列表
        val avatarUrl = intent.getStringExtra("avatar_url")
        val nickname = intent.getStringExtra("nickname")
        // 获取当前显示图片的位置
        currentPage = intent.getIntExtra("current_position", 0)
        images = intent.getStringArrayListExtra("images") ?: listOf()

        // 加载用户头像
        Glide.with(this)
            .load(avatarUrl)
            .apply(RequestOptions.circleCropTransform())
            .into(userAvatar)

        userNickname.text = nickname

        val adapter = ImagePagerAdapter(this, images)
        viewPager.adapter = adapter
        // 设置初始显示的图片位置为点击的位置
        viewPager.currentItem = currentPage
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                currentPage = position
                updatePageIndicator()
            }
            override fun onPageScrollStateChanged(state: Int) {}
        })

        // 点击事件处理
        viewPager.setOnClickListener { finish() }
        downloadButton.setOnClickListener { downloadImage() }

        updatePageIndicator()
    }

    private fun updatePageIndicator() {
        pageIndicator.text = "${currentPage + 1}/${images.size}"
    }

    private fun downloadImage() {
        val imageUrl = images[currentPage]
        val request = DownloadManager.Request(Uri.parse(imageUrl))
            .setTitle("Image Download")
            .setDescription("Downloading image...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "image_${System.currentTimeMillis()}.jpg")

        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)

        Toast.makeText(this, "图片下载完成，请相册查看", Toast.LENGTH_SHORT).show()
    }
}

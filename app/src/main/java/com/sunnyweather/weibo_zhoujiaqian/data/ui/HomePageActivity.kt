package com.sunnyweather.weibo_zhoujiaqian.data.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.sunnyweather.weibo_zhoujiaqian.R

class HomePageActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var adapter: ViewPagerAdapter
    private lateinit var tab1: ImageView
    private lateinit var tab2: ImageView
    private lateinit var bar: TextView
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)


        viewPager = findViewById(R.id.view_pager)
        tab1 = findViewById(R.id.tab1)
        tab2 = findViewById(R.id.tab2)
        bar = findViewById(R.id.toolbar)

        adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(FirstFragment(), "iH推荐")
        adapter.addFragment(SecondFragment(), "我的")
        viewPager.adapter = adapter

        tab1.setOnClickListener {
            viewPager.currentItem = 0
            updateTabSelection(0)
        }

        tab2.setOnClickListener {
            viewPager.currentItem = 1
            updateTabSelection(1)
        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                currentPage = position // 更新当前页面索引位置
                updateTabSelection(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        updateTabSelection(0)  // 默认选中第一个标签
    }

    private fun updateTabSelection(position: Int) {
        when (position) {
            0 -> {
                tab1.setImageResource(R.mipmap.blue1)
                tab2.setImageResource(R.mipmap.gray2)
                bar.setText("iH推荐")
            }
            1 -> {
                tab1.setImageResource(R.mipmap.gray1)
                tab2.setImageResource(R.mipmap.blue2)
                bar.setText("我的")
            }
        }
    }

    fun getCurrentPage(): Int {
        return currentPage
    }

    /*退出登录的调用函数
    fun showLogoutText() {
        logoutText.visibility = View.VISIBLE
    }

    fun hideLogoutText() {
        logoutText.visibility = View.GONE
    }
     */

}

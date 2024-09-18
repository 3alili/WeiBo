package com.sunnyweather.weibo_zhoujiaqian.data.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.sunnyweather.weibo_zhoujiaqian.R
import com.sunnyweather.weibo_zhoujiaqian.data.model.UserInfo
import com.sunnyweather.weibo_zhoujiaqian.data.model.UserInfoEvent
import com.sunnyweather.weibo_zhoujiaqian.data.viewmodel.LoginViewModel
import de.greenrobot.event.EventBus
import de.greenrobot.event.Subscribe
import de.greenrobot.event.ThreadMode
import kotlin.math.log

class SecondFragment : Fragment() {
    private lateinit var profileImageView: ShapeableImageView
    private lateinit var fans:TextView
    private lateinit var username:TextView
    private lateinit var text:TextView
    private var canLogin = true
    private lateinit var userInfo:UserInfo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_second, container, false)

        val imageView = view.findViewById<ImageView>(R.id.profile_image)
        imageView.setOnClickListener{
            if(canLogin) {
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                startActivity(intent)
            }
        }

        username = view.findViewById(R.id.UserName)
        // 获取 ImageView 引用
        profileImageView = view.findViewById(R.id.profile_image)
        fans = view.findViewById(R.id.Fans)
        text = view.findViewById(R.id.text)

        return view
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    fun onUserInfoEvent(event: UserInfoEvent) {
        Log.d("SecondFragment", "Received user info: ${event.userInfo}")
        userInfo = event.userInfo
        view?.findViewById<TextView>(R.id.UserName)?.text = userInfo.username
        Glide.with(this)
            .load(userInfo.avatar)
            .into(profileImageView)
        fans.text = "粉丝 9999"
        canLogin = false
        text.setText("你没有新动态哦~")

        val homePageActivity = activity as? HomePageActivity

        /*
        homePageActivity?.let {
            if (userInfo.loginStatus == true) {
                // 如果已登录，则显示退出登录的 TextView
                it.showLogoutText()
            } else {
                // 如果未登录，则隐藏退出登录的 TextView
                it.hideLogoutText()
            }
        }
         */
    }

}

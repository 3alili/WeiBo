package com.sunnyweather.weibo_zhoujiaqian.data.ui

import Adapter
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sunnyweather.weibo_zhoujiaqian.R
import com.sunnyweather.weibo_zhoujiaqian.api.AuthApiService
import com.sunnyweather.weibo_zhoujiaqian.data.model.WeiboRecord
import com.sunnyweather.weibo_zhoujiaqian.data.network.ApiClient
import com.sunnyweather.weibo_zhoujiaqian.data.repository.Repository
import com.sunnyweather.weibo_zhoujiaqian.data.viewmodel.LoginViewModel
import com.sunnyweather.weibo_zhoujiaqian.data.viewmodel.LoginViewModelFactory

class FirstFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: Adapter
    private val authService: AuthApiService by lazy { ApiClient.authService }
    private val repository: Repository by lazy { Repository(authService) }
    private lateinit var viewModel: LoginViewModel
    private lateinit var record: List<WeiboRecord>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化 ViewModel
        val viewModelFactory = LoginViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(LoginViewModel::class.java)

        // 初始化 RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 初始化 Adapter
        adapter = Adapter(requireContext(), mutableListOf())
        recyclerView.adapter = adapter

        val token = getTokenFromSharedPreferences()
        val beartoken = "Bearer $token"


        // 调用获取数据的方法
        viewModel.getHomePage(beartoken,
            onResult = { response ->
                // 处理成功的情况，更新 Adapter 的数据集
                val homePageResponse = response.body()
                homePageResponse?.data?.records?.let { records ->
                    record = records
                    adapter.weiboRecords = records.toMutableList()
                    adapter.notifyDataSetChanged()
                }
            },
            onError = { errorMessage ->
                // 处理失败的情况，可以在这里提示错误信息或者进行其他操作
                Log.e("FirstFragment", "Error: $errorMessage")
                Toast.makeText(requireContext(), "获取数据失败: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        )

        // 下拉刷新监听
        val swipeRefreshLayout: SwipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            adapter.refreshData(record)
            adapter.notifyDataSetChanged()
            // 使用 Handler 延迟1.5秒后设置 isRefreshing 为 false
            Handler(Looper.getMainLooper()).postDelayed({
                swipeRefreshLayout.isRefreshing = false
            }, 1500)
        }

        // 上拉加载更多监听
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                if (lastVisibleItemPosition == adapter.itemCount - 1) {
                    adapter.loadMoreData(null)
                }
            }
        })

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                    val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

                    for (i in firstVisiblePosition..lastVisiblePosition) {
                        val viewHolder = recyclerView.findViewHolderForAdapterPosition(i) as? Adapter.WeiboViewHolder
                        viewHolder?.let {
                            if (viewHolder.isPlaying) {
                                val rect = Rect()
                                viewHolder.itemView.getGlobalVisibleRect(rect)
                                if (rect.bottom < 0 || rect.top > recyclerView.height) {
                                    viewHolder.mediaPlayer?.pause()
                                }
                            }
                        }
                    }
                }
            }
        })
    }
    private fun getTokenFromSharedPreferences(): String {
        // 使用与 LoginActivity 中相同的 SharedPreferences 文件名和键名
        val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("token", "") ?: ""
    }

}

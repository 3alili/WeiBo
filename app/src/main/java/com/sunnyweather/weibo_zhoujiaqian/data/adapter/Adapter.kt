import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.sunnyweather.weibo_zhoujiaqian.ImagesAdapter
import com.sunnyweather.weibo_zhoujiaqian.OnImageClickListener
import com.sunnyweather.weibo_zhoujiaqian.R
import com.sunnyweather.weibo_zhoujiaqian.data.model.WeiboRecord
import com.sunnyweather.weibo_zhoujiaqian.data.ui.LoginActivity
import com.sunnyweather.weibo_zhoujiaqian.data.ui.PicActivity

class Adapter(private val context: Context, var weiboRecords: MutableList<WeiboRecord>) : RecyclerView.Adapter<Adapter.WeiboViewHolder>() {

    inner class WeiboViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.text_username)
        private val titleTextView: TextView = itemView.findViewById(R.id.text_post_content)
        private val avatarImageView: ImageView = itemView.findViewById(R.id.image_avatar)
        private val surfaceView: SurfaceView = itemView.findViewById(R.id.surface_view)
        private val imageView: ImageView = itemView.findViewById(R.id.image_view)
        private val singleView: ImageView = itemView.findViewById(R.id.single_image)
        private val imagesRecyclerView: RecyclerView = itemView.findViewById(R.id.images_recycler_view)
        private val textView: TextView = itemView.findViewById(R.id.text_view)
        private val pauseImageView: ImageView = itemView.findViewById(R.id.image_pause)
        private val videoSeekBar: SeekBar = itemView.findViewById(R.id.video_seekbar)
        private val videoTimeTextView: TextView = itemView.findViewById(R.id.video_time)
        private val likeTextView: TextView = itemView.findViewById(R.id.text_like)
        private val likeImageView: ImageView = itemView.findViewById(R.id.image_like)
        private val deleteImageView: ImageView = itemView.findViewById(R.id.image_delete)  // 删除按钮
        private val commentImageView: ImageView = itemView.findViewById(R.id.image_comment)  // 评论按钮

        var mediaPlayer: MediaPlayer? = null
        var isPlaying = false
        private var videoUrl: String? = null
        private var currentPosition = 0
        // 标志位，记录视频是否已经被点击过
        private var isFirstClick = true

        init {
            // 初始化MediaPlayer
            mediaPlayer = MediaPlayer()

            // SurfaceHolder 的回调，用于处理 SurfaceView 的生命周期
            surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    mediaPlayer?.setDisplay(holder)
                }

                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    mediaPlayer?.setDisplay(null)
                }
            })

            // 设置视频点击监听
            surfaceView.setOnClickListener {
                pauseImageView.visibility = View.VISIBLE
                pause()
            }

            // 设置暂停按钮的点击监听
            pauseImageView.setOnClickListener {
                if (isFirstClick) {
                    // 首次点击视频封面图片，隐藏暂停按钮和视频封面imageView，开始播放视频
                    pauseImageView.visibility = View.GONE
                    imageView.visibility = View.GONE
                    start()
                    isFirstClick = false // 设置为 false，表示不是第一次点击了
                } else {
                    // 非首次点击视频封面图片，可以根据需要处理
                    start()
                    pauseImageView.visibility = View.GONE
                }
            }

            //点赞按钮监听
            likeImageView.setOnClickListener {
                if (isLoggedIn()) {
                    val weiboRecord = weiboRecords[adapterPosition]
                    if (!weiboRecord.likeFlag) {
                        weiboRecord.likeCount++
                        weiboRecord.likeFlag = true
                        likeImageView.setImageResource(R.mipmap.liked) // 替换为点赞后的图片
                        likeTextView.text = weiboRecord.likeCount.toString() // 更新点赞数量
                        performLikeAnimation(likeImageView)
                    } else {
                        weiboRecord.likeCount--
                        weiboRecord.likeFlag = false
                        likeImageView.setImageResource(R.mipmap.unlike) // 替换为点赞前的图片
                        likeTextView.text = weiboRecord.likeCount.toString() // 更新点赞数量
                        performUnlikeAnimation(likeImageView)
                    }
                } else {
                    Toast.makeText(context, "请先登录后再点赞", Toast.LENGTH_SHORT).show()
                    redirectToLogin()
                }
            }


            // 评论按钮监听
            commentImageView.setOnClickListener {
                val position = adapterPosition + 1 // 列表从0开始，因此+1
                Toast.makeText(context, "点击第${position}条数据评论按钮", Toast.LENGTH_SHORT).show()
            }

            // 删除按钮监听
            deleteImageView.setOnClickListener {
                removeItem(adapterPosition)
            }

        }

        // 开始视频播放
        private fun start() {
            mediaPlayer?.apply {
                reset() // 重置 MediaPlayer
                setDataSource(videoUrl) // 设置数据源
                prepare()
                start()
                this@WeiboViewHolder.isPlaying = true
                updateSeekBar()
            }
        }

        // 暂停视频播放
        private fun pause() {
            mediaPlayer?.apply {
                pause()
                this@WeiboViewHolder.isPlaying = false
            }
        }

        fun bind(weiboRecord: WeiboRecord) {
            // 重置所有视图的可见性
            surfaceView.visibility = View.GONE
            imageView.visibility = View.GONE
            singleView.visibility = View.GONE
            imagesRecyclerView.visibility = View.GONE
            textView.visibility = View.GONE
            pauseImageView.visibility = View.GONE
            videoSeekBar.visibility = View.GONE


            usernameTextView.text = weiboRecord.username
            titleTextView.text = weiboRecord.title

            // 使用 Glide 加载头像
            Glide.with(itemView)
                .load(weiboRecord.avatar)
                .placeholder(R.drawable.gray_circle_background)
                .circleCrop()
                .into(avatarImageView)

            // 设置点赞数量
            likeTextView.text = weiboRecord.likeCount.toString()
            likeImageView.setImageResource(if (weiboRecord.likeFlag) R.mipmap.liked else R.mipmap.unlike)

            // 判断显示的视频、图片或文本模块
            when {
                weiboRecord.videoUrl != null -> {
                    // 显示视频模块
                    videoUrl = weiboRecord.videoUrl
                    surfaceView.visibility = View.VISIBLE
                    imageView.visibility = View.VISIBLE
                    pauseImageView.visibility = View.VISIBLE
                    videoSeekBar.visibility = View.VISIBLE

                    // 显示视频封面
                    Glide.with(itemView)
                        .load(weiboRecord.poster)
                        .into(imageView)

                    // 初始化时不自动播放视频
                    mediaPlayer?.reset()

                    // 设置视频总时长
                    mediaPlayer?.let {
                        it.setOnPreparedListener { mp ->
                            videoSeekBar.max = mp.duration
                            val duration = mp.duration / 1000 // 转换为秒
                            videoTimeTextView.text = formatDuration(duration)
                        }
                    }

                }
                weiboRecord.images != null && weiboRecord.images.isNotEmpty() -> {
//                    // 显示图片模块
//                    surfaceView.visibility = View.GONE
//                    videoSeekBar.visibility = View.GONE
//                    imageView.visibility = View.GONE
//                    imagesRecyclerView.visibility = View.GONE
//                    textView.visibility = View.GONE
//                    pauseImageView.visibility = View.GONE
//                    singleView.visibility = View.GONE

                    // 加载单张图片或多张图片
                    if (weiboRecord.images.size == 1) {
                        singleView.visibility = View.VISIBLE
                        Glide.with(singleView)
                            .asBitmap()
                            .load(weiboRecord.images[0])
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    val aspectRatio = resource.width.toFloat() / resource.height.toFloat()
                                    val layoutParams = singleView.layoutParams

                                    // Set the layout height based on aspect ratio
                                    layoutParams.height = (singleView.width / aspectRatio).toInt()
                                    singleView.layoutParams = layoutParams

                                    // Set the bitmap to ImageView
                                    singleView.setImageBitmap(resource)
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                }
                            })
                        singleView.setOnClickListener {
                            val position = adapterPosition
                            val weiboRecord = weiboRecords[position]
                            // 获取当前图片的所有图片列表
                            val images = weiboRecord.images ?: return@setOnClickListener

                            // 构建启动 PicActivity 所需的参数
                            val intent = Intent(context, PicActivity::class.java).apply {
                                putExtra("avatar_url", weiboRecord.avatar)
                                putExtra("nickname", weiboRecord.username)
                                putStringArrayListExtra("images", ArrayList(images))
                                putExtra("current_position", position) // 传递当前点击的图片位置
                            }
                            context.startActivity(intent)
                        }
                    } else {
                        imagesRecyclerView.visibility = View.VISIBLE
                        imagesRecyclerView.layoutManager = GridLayoutManager(itemView.context, 3)
                        imagesRecyclerView.adapter = ImagesAdapter(weiboRecord.images, object :
                            OnImageClickListener{
                            override fun onImageClick(images: List<String>, position: Int) {
                                val intent = Intent(context, PicActivity::class.java).apply {
                                    putExtra("avatar_url", weiboRecord.avatar)
                                    putExtra("nickname", weiboRecord.username)
                                    putStringArrayListExtra("images", ArrayList(images))
                                    putExtra("current_position", position)
                                }
                                context.startActivity(intent)
                            }
                        })
                    }
                }
                else -> {
                    // 显示普通文本模块
                    textView.visibility = View.VISIBLE
                    textView.text = weiboRecord.title
                }
            }
        }

        private fun updateSeekBar() {
            mediaPlayer?.let { mp ->
                videoSeekBar.max = mp.duration
                videoSeekBar.progress = mp.currentPosition
                if (isPlaying) {
                    videoSeekBar.postDelayed({ updateSeekBar() }, 1000)
                    val currentPosition = mp.currentPosition / 1000 // 转换为秒
                    videoTimeTextView.text = formatDuration(currentPosition)
                }
            }
        }

        // 将秒数格式化为时间显示格式
        private fun formatDuration(seconds: Int): String {
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return String.format("%02d:%02d", minutes, remainingSeconds)
        }
    }


    fun refreshData(newData: List<WeiboRecord>) {
        // 将 newData 列表顺序打乱，模拟随机顺序
        val shuffledData = newData.toMutableList()
        shuffledData.shuffle()
        weiboRecords.clear()
        weiboRecords.addAll(shuffledData)
    }

    fun loadMoreData(newData: List<WeiboRecord>?) {
        if (newData.isNullOrEmpty()) {
            Toast.makeText(context, "无更多内容", Toast.LENGTH_SHORT).show()
        } else {
            weiboRecords.addAll(newData)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeiboViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return WeiboViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeiboViewHolder, position: Int) {
        val weiboRecord = weiboRecords[position]
        holder.bind(weiboRecord)
    }

    override fun getItemCount(): Int {
        return weiboRecords.size
    }


    // 释放 MediaPlayer 资源
    override fun onViewRecycled(holder: WeiboViewHolder) {
        super.onViewRecycled(holder)
        // 停止视频播放
    }

    private fun isLoggedIn(): Boolean {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("login_status", false)
    }

    private fun redirectToLogin() {
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
    }
    private fun performLikeAnimation(view: View) {
        val scaleXUp = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.2f, 1.0f)
        val scaleYUp = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.2f, 1.0f)
        val rotate = ObjectAnimator.ofFloat(view, "rotationY", 0f, 360f)
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleXUp, scaleYUp, rotate)
        animatorSet.duration = 1000
        animatorSet.start()
    }

    private fun performUnlikeAnimation(view: View) {
        val scaleXDown = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0.8f, 1.0f)
        val scaleYDown = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0.8f, 1.0f)
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleXDown, scaleYDown)
        animatorSet.duration = 1000
        animatorSet.start()
    }

    fun removeItem(position: Int) {
        weiboRecords.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, weiboRecords.size)
    }
}


// ImagePagerAdapter.kt
package com.example.imageviewer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.sunnyweather.weibo_zhoujiaqian.R

class ImagePagerAdapter(private val context: Context, private val images: List<String>) : PagerAdapter() {

    override fun getCount(): Int {
        return images.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.detail_img, container, false)
        val imageView = view.findViewById<ImageView>(R.id.image_view)

        Glide.with(context)
            .load(images[position])
            .into(imageView)

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}

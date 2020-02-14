package com.jiwoolee.memoappchallenge

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val bundle = intent.extras
        if(bundle != null){
            val title = bundle.getString("title")
            val content = bundle.getString("content")
            val images = bundle.getStringArrayList("images")
            tv_detail_text.text = "$title, $content"

            val array: ByteArray = Base64.decode(images?.get(0), Base64.DEFAULT)
            Glide.with(this)
                .load(array)
                .fitCenter()
                .into(iv_detail_image)
        }
    }
}
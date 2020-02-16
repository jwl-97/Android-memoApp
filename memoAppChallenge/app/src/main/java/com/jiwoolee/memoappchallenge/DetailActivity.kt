package com.jiwoolee.memoappchallenge

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.jiwoolee.memoappchallenge.room.MemoDB
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {
    private var memoDb: MemoDB? = null
    private var id: Long = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val bundle = intent.extras
        if(bundle != null){
            val title = bundle.getString("title")
            val content = bundle.getString("content")
            val images = bundle.getStringArrayList("images")
            id = bundle.getLong("id")
            tv_detail_title.text = title
            tv_detail_content.text = content

            val array: ByteArray = Base64.decode(images?.get(0), Base64.DEFAULT)
            Glide.with(this)
                .load(array)
                .fitCenter()
                .into(iv_detail_image)
        }
        memoDb = MemoDB.getInstance(this)

        btn_delete.setOnClickListener {
            val addThread = Thread(Runnable {
                memoDb?.memoDao()?.deleteById(id)
            })
            addThread.start()

            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

            finish()
        }
    }
}
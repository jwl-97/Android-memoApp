package com.jiwoolee.memoappchallenge

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jiwoolee.memoappchallenge.room.Memo
import com.jiwoolee.memoappchallenge.room.MemoDB
import kotlinx.android.synthetic.main.activity_add.*

class AddActivity : AppCompatActivity() {
    private var memoDb : MemoDB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        memoDb = MemoDB.getInstance(this)
        val r = Runnable {
            val newCat = Memo()
            newCat.memoTitle = et_add_title.text.toString()
            newCat.memoContent = et_add_content.text.toString()
            newCat.memoImages = listOf(et_add_images.text.toString())
            memoDb?.memoDao()?.insert(newCat)
        }

        btn_add.setOnClickListener {
            val addThread = Thread(r)
            addThread.start()

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        MemoDB.destroyInstance()
        super.onDestroy()
    }
}
package com.jiwoolee.memoappchallenge

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jiwoolee.memoappchallenge.room.Memo
import com.jiwoolee.memoappchallenge.room.MemoDB
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(){
    private var memoDb : MemoDB? = null
    private var memoList = listOf<Memo>()
    private lateinit var mAdapter : RecyclerviewAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        memoDb = MemoDB.getInstance(this)
        mAdapter = RecyclerviewAdapter(this, memoList)
        recyclerView = findViewById(R.id.recyclerView)

        val r = Runnable {
            try {
                memoList = memoDb?.memoDao()?.getAll()!!
                mAdapter = RecyclerviewAdapter(this, memoList)
                mAdapter.notifyDataSetChanged()

                recyclerView.adapter = mAdapter
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.setHasFixedSize(true)
            } catch (e: Exception) {
                Log.d("ljwLog", e.toString())
            }
        }

        val thread = Thread(r)
        thread.start()

        btn_toAddActivity.setOnClickListener {
            startActivity(Intent(this, AddActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        MemoDB.destroyInstance()
        memoDb = null
        super.onDestroy()
    }
}

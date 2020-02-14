package com.jiwoolee.memoappchallenge

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jiwoolee.memoappchallenge.room.Memo
import com.jiwoolee.memoappchallenge.room.MemoDB
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OnItemClick{
    private var memoDb: MemoDB? = null
    private var memoList : ArrayList<Memo> = arrayListOf<Memo>()
    private lateinit var mAdapter: RecyclerviewAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)

        memoDb = MemoDB.getInstance(this)

        val loadThread = Thread(Runnable {
            try {
                memoList = memoDb?.memoDao()?.getAll() as ArrayList<Memo>
                setRecyclerviewAdapter()
            } catch (e: Exception) {
                Log.d("ljwLog", "memoDao()?.getAll()_err : $e")
            }
        })
        loadThread.start()

        try {
            loadThread.join() //Thread안에서 선행 작업 실행 완료 후 다음 작업 수행(순차적)
        } catch (e: java.lang.Exception) {
            Log.d("ljwLog", "loadThread.join()_err : $e")
        }

        btn_toAddActivity.setOnClickListener {
            startActivity(Intent(this, AddActivity::class.java))
        }
    }

    private fun setRecyclerviewAdapter() {
        mAdapter = RecyclerviewAdapter(this, memoList, this)
        mAdapter.notifyDataSetChanged()
        recyclerView.adapter = mAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onDestroy() {
        MemoDB.destroyInstance()
        memoDb = null
        super.onDestroy()
    }

    override fun onClick(memoItem : Memo) {
        val intent = Intent(applicationContext, DetailActivity::class.java)

        val bundle = Bundle()
        bundle.putString("title", memoItem.memoTitle)
        bundle.putString("content", memoItem.memoContent)
        bundle.putStringArrayList("images", memoItem.memoImages)
        intent.putExtras(bundle)

        startActivity(intent)
    }
}

package com.jiwoolee.memoappchallenge

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jiwoolee.memoappchallenge.adapter.OnItemClick
import com.jiwoolee.memoappchallenge.adapter.RecyclerviewAdapter
import com.jiwoolee.memoappchallenge.databinding.ActivityMainBinding
import com.jiwoolee.memoappchallenge.room.Memo
import com.jiwoolee.memoappchallenge.room.MemoDB
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OnItemClick {
    private var memoDb: MemoDB? = null
    private var memoList : ArrayList<Memo> = arrayListOf<Memo>()
    private lateinit var mAdapter: RecyclerviewAdapter
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        memoDb = MemoDB.getInstance(this)

        getItemFromDb()

        fab_toAddActivity.setOnClickListener {
            startActivity(Intent(this, AddActivity::class.java))
        }
    }

    private fun getItemFromDb(){
        val loadThread = Thread(Runnable {
            try {
                memoList = memoDb?.memoDao()?.getAll() as ArrayList<Memo> //SELECT ALL
                setRecyclerviewAdapter()
            } catch (e: Exception) {
                Log.d("ljwLog", "MainActivity_getAll()_err : $e")
            }
        })
        loadThread.start()

        try {
            loadThread.join() //Thread안에서 선행 작업 실행 완료 후 다음 작업 수행(순차적)
        } catch (e: java.lang.Exception) {
            Log.d("ljwLog", "MainActivity_loadThread.join()_err : $e")
        }
    }

    private fun setRecyclerviewAdapter() {
        mAdapter = RecyclerviewAdapter(memoList, this)
        mAdapter.notifyDataSetChanged()
        binding.rvMain.adapter = mAdapter
        binding.rvMain.layoutManager = LinearLayoutManager(this) as RecyclerView.LayoutManager?
    }

    //뒤로가기 버튼을 두번 연속으로 눌러야 종료
    private var time: Long = 0
    override fun onBackPressed() {
        if (System.currentTimeMillis() - time >= 2000) {
            time = System.currentTimeMillis()
            Toast.makeText(applicationContext, "뒤로 버튼을 한번 더 누르면 종료합니다.", Toast.LENGTH_SHORT).show();
        } else if (System.currentTimeMillis() - time < 2000) {
            finish()
        }
    }

    override fun onDestroy() {
        MemoDB.destroyInstance()
        memoDb = null
        super.onDestroy()
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //OnItemClick
    override fun onClick(memoItem : Memo) {
        val intent = Intent(applicationContext, DetailActivity::class.java)

        val bundle = Bundle()
        bundle.putSerializable("memo", memoItem)
        intent.putExtras(bundle)

        startActivity(intent)
    }
}

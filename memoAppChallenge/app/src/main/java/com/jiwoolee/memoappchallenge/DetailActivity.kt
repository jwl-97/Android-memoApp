package com.jiwoolee.memoappchallenge

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.jiwoolee.memoappchallenge.room.Memo
import com.jiwoolee.memoappchallenge.room.MemoDB
import kotlinx.android.synthetic.main.activity_detail.*

;

/*
메모 상세보기 화면
 */
class DetailActivity : AppCompatActivity(), View.OnClickListener {
    private var memoDb: MemoDB? = null
    private var id: Long? = 0
    private var images: ArrayList<String>? = null
    private var memoList: Memo? = null
    private val REQUEST_EDIT = 1000

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        memoDb = MemoDB.getInstance(this)

        val bundle = intent.extras
        if (bundle != null) {
            memoList = bundle.getSerializable(("memo")) as Memo?
            setData(memoList)
        }

        btn_detail_view_delete.setOnClickListener(this)
        btn_detail_view_edit.setOnClickListener(this)
    }

    fun setData(memoList : Memo?){
        id = memoList?.id
        images = memoList?.memoImages

        tv_detail_view_title.text = memoList?.memoTitle
        tv_detail_view_content.text = memoList?.memoContent

        val array: ByteArray = Base64.decode(images?.get(0), Base64.DEFAULT)
        Glide.with(this)
            .load(array)
            .fitCenter()
            .into(iv_detail_view_image)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_detail_view_delete -> { //삭제
                Thread(Runnable {
                    memoDb?.memoDao()?.deleteById(id)  //DELETE
                }).start()

                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)

                finish()
            }

            R.id.btn_detail_view_edit -> { //편집
                val intent = Intent(applicationContext, AddActivity::class.java)
                val bundle = Bundle()
                bundle.putSerializable("memo", memoList)
                intent.putExtras(bundle)
                startActivityForResult(intent, REQUEST_EDIT)
            }
        }
    }

    override fun onBackPressed() {
        intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_EDIT){ //편집 완료시
            memoList = data?.getSerializableExtra("memo") as Memo?
            setData(memoList)
        }
    }

    override fun onDestroy() {
        MemoDB.destroyInstance()
        memoDb = null
        super.onDestroy()
    }
}
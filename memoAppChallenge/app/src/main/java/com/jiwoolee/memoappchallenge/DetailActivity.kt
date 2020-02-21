package com.jiwoolee.memoappchallenge

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.jiwoolee.memoappchallenge.room.Memo
import com.jiwoolee.memoappchallenge.room.MemoDB
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.item_image.view.*

/*
메모 상세보기 화면
 */
class DetailActivity : AppCompatActivity(), View.OnClickListener {
    private val REQUEST_EDIT = 1000

    private var memoDb: MemoDB? = null
    private var id: Long? = 0
    private var memoList: Memo? = null
    private lateinit var imagesContainer : ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        memoDb = MemoDB.getInstance(this)
        imagesContainer = findViewById(R.id.detail_images_container)

        val bundle : Bundle? = intent.extras //MainActivity에서 Recyclerview item 클릭시 (RecyclerviewAdapter -> MainActivity -> DetailActivity)
        if (bundle != null) {
            memoList = bundle.getSerializable(("memo")) as Memo?
            setDataToForm(memoList)
        }

        ib_detail_view_delete.setOnClickListener(this)
        ib_detail_view_edit.setOnClickListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_EDIT){ //편집 완료시 (DetailActivity -> AddActivity -> DetailActivity)
            memoList = data?.getSerializableExtra("memo") as Memo?
            setDataToForm(memoList)
        }
    }

    private fun setDataToForm(memoList : Memo?){
        imagesContainer.removeAllViews()

        id = memoList?.id
        tv_detail_view_title.text = memoList?.memoTitle
        tv_detail_view_content.text = memoList?.memoContent

        val images = memoList?.memoImages

        if (images != null && images[0] != "") {
            for (image in images) {
                val imageHolder = LayoutInflater.from(this).inflate(R.layout.item_image, null)
                val thumbnail = imageHolder.iv_images

                val array: ByteArray = Base64.decode(image, Base64.DEFAULT)
                Glide.with(this)
                    .load(array)
                    .fitCenter()
                    .into(thumbnail)

                imagesContainer.addView(imageHolder)
                thumbnail.layoutParams = FrameLayout.LayoutParams(300, 400)
                thumbnail.setPadding(10)
                thumbnail.setBackgroundResource(R.color.transparent)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ib_detail_view_delete -> { //삭제
                Thread(Runnable {
                    memoDb?.memoDao()?.deleteById(id)  //DELETE
                }).start()

                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)

                finish()
            }

            R.id.ib_detail_view_edit -> { //편집화면으로 이동
                val intent = Intent(this, AddActivity::class.java)

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

    override fun onDestroy() {
        MemoDB.destroyInstance()
        memoDb = null
        super.onDestroy()
    }
}
package com.jiwoolee.memoappchallenge

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.jiwoolee.memoappchallenge.room.Memo
import com.jiwoolee.memoappchallenge.room.MemoDB
import kotlinx.android.synthetic.main.activity_add.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream


class AddActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener{
    private var memoDb : MemoDB? = null
    private var newMemo = Memo()
    private var memoLIst : MutableList<String> = mutableListOf<String>()
    private lateinit var r : Runnable
    private var isCamera: Boolean? = false

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var mContext: Context
        private const val PICK_FROM_ALBUM = 1
        private const val PICK_FROM_CAMERA = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        mContext = this

        requestPermissions() //권한요청

        memoDb = MemoDB.getInstance(this)
        r = Runnable {
            newMemo.memoTitle = et_add_title.text.toString()
            newMemo.memoContent = et_add_content.text.toString()
            if(memoLIst.isEmpty()){
                newMemo.memoImages = listOf("")
            }else{
                newMemo.memoImages = memoLIst
            }

            memoDb?.memoDao()?.insert(newMemo)
        }

        btn_add.setOnClickListener(this)
        btn_cancel.setOnClickListener(this)
        btn_addImages.setOnClickListener(this)
        btn_addImages.setOnLongClickListener(this)

    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) { //중간에 취소시
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        when (requestCode) {
            PICK_FROM_ALBUM -> {
                val photoUri: Uri? = data!!.data
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
                btn_addImages.setImageBitmap(bitmap) //이미지뷰에 적용

                val stream = ByteArrayOutputStream() //bitmap->byteArray->base64로 db list에 저장
                bitmap!!.compress(Bitmap.CompressFormat.PNG, 90, stream)
                val image = stream.toByteArray()
                val saveThis: String = Base64.encodeToString(image, Base64.DEFAULT)
                memoLIst.add(saveThis)

//                val linearLayout = findViewById<ViewGroup>(R.id.linearLayoutID)
//                val bt = ImageButton(this)
//                bt.setBackgroundResource(R.drawable.ic_addbox)
//                bt.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//                linearLayout.addView(bt)
//                bt.setOnClickListener {
//                    registerPictures()
//                }
            }
        }
    }

    //권한요청
    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 6.0 마쉬멜로우 이상일 경우에는 권한 체크 후 권한 요청
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d("ljwLog", "권한 설정 완료")
            } else {
                Log.d("ljwLog", "권한 설정 요청")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }
    }

    // 권한
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("ljwLog", "onRequestPermissionsResult")
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.d("ljwLog", "Permission: " + permissions[0] + "was " + grantResults[0])
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_add ->{
                val addThread = Thread(r)
                addThread.start()

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            R.id.btn_cancel -> finish()
            R.id.btn_addImages -> registerPictures() //카메라or앨범 선택
        }
    }

    override fun onLongClick(v: View?): Boolean {
        when (v?.id) {
            R.id.btn_addImages -> {
                Toast.makeText(mContext, "롱클릭", Toast.LENGTH_LONG).show()
                btn_addImages.setImageResource(R.drawable.ic_addbox) //이미지뷰에 적용
                memoLIst.removeAt(0)
            }
        }
        return true
    }

    //카메라or앨범 선택
    private fun registerPictures() {
        val builder = AlertDialog.Builder(mContext)
        builder.setTitle("사진 추가")

        builder.setItems(R.array.LAN) { _, pos ->
            when (pos) {
                0 -> goAlbum()
//                1 -> takePhoto()
//                2 ->
            }
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    //앨범에서 이미지 가져오기
    private fun goAlbum() {
        isCamera = false

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, PICK_FROM_ALBUM)
    }

    override fun onDestroy() {
        MemoDB.destroyInstance()
        super.onDestroy()
    }
}
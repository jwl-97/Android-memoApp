package com.jiwoolee.memoappchallenge

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.jiwoolee.memoappchallenge.room.Memo
import com.jiwoolee.memoappchallenge.room.MemoDB
import kotlinx.android.synthetic.main.activity_add.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {
    private var memoDb: MemoDB? = null
    private var newMemo = Memo()
    private var memoImageLIst: ArrayList<String> = ArrayList<String>()
    private lateinit var r: Runnable

    private var isCamera: Boolean? = false
    private var tempFile: File? = null

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
            if (memoImageLIst.isEmpty()) {
                newMemo.memoImages = arrayListOf("")
            } else {
                newMemo.memoImages = memoImageLIst
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
            if (tempFile != null) {
                if (tempFile!!.exists()) {
                    if (tempFile!!.delete()) {
                        tempFile = null
                    }
                }
            }
            return
        }

        when (requestCode) {
            PICK_FROM_ALBUM -> {
                setImageToImagebutton(data!!.data!!)
//                Log.d("ljwLog", memoLIst.toString())
//                Log.d("ljwLog", "size : "+memoLIst.size.toString())

//                val linearLayout = findViewById<ViewGroup>(R.id.linearLayoutID)
//                val bt = ImageButton(this)
//                bt.setBackgroundResource(R.drawable.ic_addbox)
//                bt.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//                linearLayout.addView(bt)
//                bt.setOnClickListener {
//                    registerPictures()
//                }
            }

            PICK_FROM_CAMERA -> {
                setImageToImagebutton(Uri.fromFile(tempFile))
            }
        }
    }

    //이미지 파일 다루기
    private fun setImageToImagebutton(photoUri : Uri){
        val bitmap : Bitmap
        val options = BitmapFactory.Options()
        options.inSampleSize = 4

//        if(isCamera == true){
//            bitmap = rotateImage(BitmapFactory.decodeStream(contentResolver.openInputStream(photoUri), null, options)!!, 90)
//        }else{
//            bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(photoUri), null, options)!!
//        }

        bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(photoUri), null, options)!!

        val resizedBitmap = resizeBitmap(bitmap, 300, 400)
        btn_addImages.setImageBitmap(resizedBitmap)             //이미지버튼에 적용
        memoImageLIst.add(convertBitmapToBase64(resizedBitmap)) //bitmap을 base64로 변환 -> 리스트에 추가 -> (db에 저장)
    }

    private fun convertBitmapToBase64(resizedBitmap : Bitmap) : String{
        val stream = ByteArrayOutputStream()                    //bitmap->byteArray->base64
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val image = stream.toByteArray()
        return Base64.encodeToString(image, Base64.DEFAULT)
    }

    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    //사진회전
    private fun rotateImage(source: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    //listener
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_add -> {
                val addThread = Thread(r)
                addThread.start()

                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)

                finish()
            }
            R.id.btn_cancel -> finish()
            R.id.btn_addImages -> registerPictures() //카메라or앨범 선택
        }
    }

    override fun onLongClick(v: View?): Boolean { //롱클릭시 이미지 삭제
        when (v?.id) {
            R.id.btn_addImages -> {
                Toast.makeText(mContext, "롱클릭", Toast.LENGTH_LONG).show()
                btn_addImages.setImageResource(R.drawable.ic_addbox) //이미지버튼에 적용
                memoImageLIst.removeAt(0) //리스트에서 삭제
            }
        }
        return true
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //카메라or앨범 선택
    private fun registerPictures() {
        val builder = AlertDialog.Builder(mContext)
        builder.setTitle("사진 추가")

        builder.setItems(R.array.LAN) { _, pos ->
            when (pos) {
                0 -> goAlbum()
                1 -> takePhoto()
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

    //카메라에서 이미지 가져오기
    private fun takePhoto() {
        isCamera = true

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        try {
            tempFile = createImageFile()
        } catch (e: IOException) {
            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            finish()
            e.printStackTrace()
        }

        if (tempFile != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val photoUri = FileProvider.getUriForFile(this, "com.jiwoolee.memoappchallenge.fileprovider", tempFile!!)/////
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, PICK_FROM_CAMERA)
            } else {
                val photoUri = Uri.fromFile(tempFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, PICK_FROM_CAMERA)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val filePrefix = "img_" + timeStamp + "_";
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(filePrefix, ".jpg", storageDir)
        return image
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //권한요청
    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 6.0 마쉬멜로우 이상일 경우에는 권한 체크 후 권한 요청
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
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

    override fun onDestroy() {
        MemoDB.destroyInstance()
        super.onDestroy()
    }
}
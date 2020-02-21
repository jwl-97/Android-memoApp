package com.jiwoolee.memoappchallenge

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.jiwoolee.memoappchallenge.room.Memo
import com.jiwoolee.memoappchallenge.room.MemoDB
import kotlinx.android.synthetic.main.activity_add.*
import kotlinx.android.synthetic.main.item_image.view.*
import java.io.*
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Base64
import androidx.core.view.setPadding

/*
메모 추가/편집 화면
 */
class AddActivity : AppCompatActivity(), View.OnClickListener {
    private var memoDb: MemoDB? = null
    private var newMemo = Memo()
    private var memoImageLIst: ArrayList<String> = ArrayList()
    private var tempImageLIst: ArrayList<String> = ArrayList()

    private var isUrl: Boolean = false
    private var tempFile: File? = null
    private var mCurrentPhotoPath: String = ""
    private lateinit var alertDialog: AlertDialog
    private lateinit var handler: DisplayHandler

    private lateinit var imagesContainer: ViewGroup
    private var tempPosition : Int = 0

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
        imagesContainer = findViewById(R.id.add_images_container)
        memoDb = MemoDB.getInstance(this)

        val bundle = intent.extras //DetailActivity에서 편집 클릭시 (DetailActivity -> AddActivity)
        if (bundle != null) {
            setEditModeVisible(true)

            newMemo = bundle.getSerializable(("memo")) as Memo
            setDataToForm(newMemo)
        }

        ib_add_ok.setOnClickListener(this)
        ib_add_cancel.setOnClickListener(this)
        ib_add_edit.setOnClickListener(this)
        btn_add_Images.setOnClickListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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
                mCurrentPhotoPath = getRealPathFromURI(data!!.data!!)
                setImage(data.data!!)
            }
            PICK_FROM_CAMERA -> setImage(Uri.fromFile(tempFile))
        }
    }

    //이미지처리
    private fun setImage(photoUri: Uri) {
        val bitmap = makeBitmap(contentResolver.openInputStream(photoUri))
        setImageToButtonAndSave(bitmap)
    }

    private fun makeBitmap(inputStream: InputStream?): Bitmap {
        val options = BitmapFactory.Options()
        options.inSampleSize = 8
        return BitmapFactory.decodeStream(inputStream, null, options)!!
    }

    private fun setImageToButtonAndSave(bitmap: Bitmap) {
        val rotatedBitmap: Bitmap = if(!isUrl) getRotatedBitmap(mCurrentPhotoPath, bitmap) else bitmap
        val resizedBitmap = resizeBitmap(rotatedBitmap, 300, 400)

        val imageHolder = LayoutInflater.from(this).inflate(R.layout.item_image, null)
        val thumbnail = imageHolder.iv_images

        thumbnail.setImageBitmap(resizedBitmap)

        imagesContainer.addView(imageHolder)
        thumbnail.id = tempPosition
        thumbnail.layoutParams = FrameLayout.LayoutParams(300, 400)
        thumbnail.setPadding(10)
        thumbnail.setBackgroundResource(R.color.transparent)

        tempImageLIst.add(convertBitmapToBase64(resizedBitmap))

        thumbnail.setOnLongClickListener { //롱클릭시 이미지 삭제
//            Toast.makeText(this, "delete "+thumbnail.id, Toast.LENGTH_LONG).show()
            tempImageLIst.removeAt(thumbnail.id)
            imagesContainer.removeView(imageHolder)
            true
        }
        tempPosition++
    }

    private fun convertBitmapToBase64(resizedBitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()                    //bitmap->byteArray->base64
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val image = stream.toByteArray()
        return Base64.encodeToString(image, Base64.DEFAULT)
    }

    //사진회전, 리사이즈
    private fun getRotatedBitmap(path: String, bitmap: Bitmap): Bitmap {
        val ei = ExifInterface(path)
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1) //각도구하기

        return when (orientation) { //각도에 따른 회전
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270)
            else -> bitmap
        }
    }

    private fun rotateImage(source: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private fun setDataToForm(newMemo: Memo) {
        tempPosition = 0

        et_add_title.text = Editable.Factory.getInstance().newEditable(newMemo.memoTitle)
        et_add_content.text = Editable.Factory.getInstance().newEditable(newMemo.memoContent)

        val images: List<String>? = newMemo.memoImages
        memoImageLIst = arrayListOf()
        if (images != null) {
            memoImageLIst.addAll(images)
            if (memoImageLIst.isNotEmpty() && memoImageLIst[0] == "") {
                memoImageLIst.removeAt(0)
            }
        }

        if (images != null && images[0] != "") {
            var position = 0
            for (image in images) {
                val imageHolder = LayoutInflater.from(this).inflate(R.layout.item_image, null)
                val thumbnail = imageHolder.iv_images
                thumbnail.id = position

                val array: ByteArray = Base64.decode(image, Base64.DEFAULT)
                Glide.with(this)
                    .load(array)
                    .fitCenter()
                    .into(thumbnail)

                imagesContainer.addView(imageHolder)
                thumbnail.layoutParams = FrameLayout.LayoutParams(300, 400)
                thumbnail.setPadding(10)
                thumbnail.setBackgroundResource(R.color.transparent)

                thumbnail.setOnLongClickListener { //롱클릭시 이미지 삭제
//                    Toast.makeText(this, thumbnail.id.toString(), Toast.LENGTH_LONG).show()
                    if(thumbnail.id == 0){
                        memoImageLIst[0] = ""
                    }else{
                        memoImageLIst.removeAt(position - 1)
                    }
                    imagesContainer.removeView(imageHolder)
                    true
                }
                position++
            }
        }
    }

    //listener
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ib_add_ok -> { //추가
                val insertThread = Thread(Runnable {
                    try {
                        storeItemToMemo()
                        memoDb?.memoDao()?.insert(newMemo) //INSERT
                    } catch (e: Exception) {
                        Log.d("ljwLog", "AddActivity_insert_err : $e")
                    }
                })
                insertThread.start()

                try {
                    insertThread.join() //Thread안에서 선행 작업 실행 완료 후 다음 작업 수행(순차적)
                } catch (e: java.lang.Exception) {
                    Log.d("ljwLog", "AddActivity_insertThread.join()_err : $e")
                }

                intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)

                finish()
            }

            R.id.ib_add_edit -> { //편집
                val updateThread = Thread(Runnable {
                    try {
                        storeItemToMemo()
                        memoDb?.memoDao()?.update(newMemo) //UPDATE
                    } catch (e: Exception) {
                        Log.d("ljwLog", "AddActivity_update_err : $e")
                    }
                })
                updateThread.start()

                try {
                    updateThread.join() //Thread안에서 선행 작업 실행 완료 후 다음 작업 수행(순차적)
                } catch (e: java.lang.Exception) {
                    Log.d("ljwLog", "AddActivity_updateThread.join()_err : $e")
                }

                intent = Intent(applicationContext, AddActivity::class.java)
                val bundle = Bundle()
                bundle.putSerializable("memo", newMemo)
                intent.putExtras(bundle)

                setResult(Activity.RESULT_OK, intent)
                finish()

                setEditModeVisible(false)
            }

            R.id.ib_add_cancel -> finish()
            R.id.btn_add_Images -> selectRegisterImagesType() //카메라or앨범orURL 선택
        }
    }

    //DB에 저장할 객체 수정하기
    private fun storeItemToMemo() {
        newMemo.memoTitle = et_add_title.text.toString()
        newMemo.memoContent = et_add_content.text.toString()
        if(tempImageLIst.isNotEmpty()){
            memoImageLIst.addAll(tempImageLIst)
        }

        if (memoImageLIst.isEmpty()) {
            memoImageLIst = arrayListOf("")
        } else if (memoImageLIst[0] == "") {
            if(memoImageLIst.size == 1){
                memoImageLIst = arrayListOf("")
            }else{
                memoImageLIst.removeAt(0)
            }
        }
        newMemo.memoImages = memoImageLIst
    }

    private fun setEditModeVisible(boolean: Boolean) {
        if (boolean) {
            ib_add_edit.visibility = View.VISIBLE
            ib_add_ok.visibility = View.GONE
        } else {
            ib_add_edit.visibility = View.GONE
            ib_add_ok.visibility = View.VISIBLE
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //카메라or앨범orURL 선택
    private fun selectRegisterImagesType() {
        val builder = AlertDialog.Builder(mContext)
        builder.setTitle("사진 추가")

        builder.setItems(R.array.TYPE) { _, pos ->
            when (pos) {
                0 -> imageFromAlbum() //앨범
                1 -> imageFromCamera() //카메라
                2 -> getUrlLink() //URL링크
            }
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    //앨범에서 이미지 가져오기
    private fun imageFromAlbum() {
        isUrl = false

        intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, PICK_FROM_ALBUM)
    }

    //카메라에서 이미지 가져오기
    private fun imageFromCamera() {
        isUrl = false
        intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        handler = DisplayHandler()

        try {
            tempFile = createImageFile()
        } catch (e: IOException) {
            Log.d("ljwLog", "AddActivity_imageFromCamera_IOException_err : $e")

            val msg = Message()
            msg.what = -1
            msg.obj = "이미지 처리 오류! 다시 시도해주세요."
            handler.sendMessage(msg) //Toast

            finish()
        }

        if (tempFile != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val photoUri = FileProvider.getUriForFile(this, "com.jiwoolee.memoappchallenge.fileprovider", tempFile!!)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, PICK_FROM_CAMERA)
            } else {
                val photoUri = Uri.fromFile(tempFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, PICK_FROM_CAMERA)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val filePrefix = "img_" + timeStamp + "_";
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(filePrefix, ".jpg", storageDir)
        mCurrentPhotoPath = image.absolutePath
        return image
    }

    private fun getRealPathFromURI(uri : Uri) : String {
        var index = 0
        val proj : Array<String> = arrayOf(MediaStore.Images.Media.DATA)

        val cursor: Cursor = contentResolver.query(uri, proj, null, null, null)!!
        if (cursor.moveToFirst()) {
            index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        }

        return cursor.getString(index)
    }

    //URL링크를 통해 이미지 가져오기
    private fun getUrlLink() {
        isUrl = true

        val et = EditText(mContext)
        et.setText(R.string.test_url_link)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("외부 이미지 주소(URL)")
            .setMessage("URL을 입력하세요")
            .setView(et)
            .setPositiveButton("확인") { _, _ ->
                val value = et.text.toString()
                imageFromUrlLink(value)
            }
            .setNegativeButton("취소") { _, _ -> }

        alertDialog = builder.create()
        alertDialog.show()
    }

    private fun imageFromUrlLink(url: String) {
        handler = DisplayHandler()

        var isOk = true
        lateinit var bitmap: Bitmap

        val getThread = Thread(Runnable {
            try {
                val conn = URL(url).openConnection()
                conn.doInput = true
                conn.connect()
                val inputStream = conn.getInputStream()
                bitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: IOException) {
                //UnknownHostException, FileNotFoundException
                Log.d("ljwLog", "AddActivity_imageFromUrlLink_UnknownHostException_err : $e")

                val msg = Message()
                msg.what = -1
                msg.obj = "URL주소 혹은 네트워크 상태를 확인해주세요."
                handler.sendMessage(msg) //Toast

                alertDialog.dismiss()
                isOk = false
            }
        })
        getThread.start()

        try {
            getThread.join() //Thread안에서 선행 작업 실행 완료 후 다음 작업 수행(순차적)
        } catch (e: java.lang.Exception) {
            Log.d("ljwLog", "AddActivity_getThread.join()_err : $e")
        }

        if (isOk) {
            setImageToButtonAndSave(bitmap)
        }
    }

    @SuppressLint("HandlerLeak")
    inner class DisplayHandler : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == -1) {
                Toast.makeText(mContext, msg.obj.toString(), Toast.LENGTH_SHORT).show()
            }
        }
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("ljwLog", "onRequestPermissionsResult")
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.d("ljwLog", "Permission: " + permissions[0] + "was " + grantResults[0])
        }
    }

    override fun onDestroy() {
        MemoDB.destroyInstance()
        memoDb = null
        super.onDestroy()
    }
}
package com.jasen.kimjaeseung.morningbees.missioncreate

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import java.io.FileOutputStream
import java.io.OutputStream


import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.main.MainActivity
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.URIPathHelper
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_mission_create.*

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

class MissionCreateActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var accessToken: String
    val service = MorningBeesService.create()
    var difficulty: Int = -1
    var description: String = ""
    val type: Int = 1
    private var beeId: Int = 0
    var tempFile: File? = null     // 카메라로 찍은 사진 File (갤러리에 저장)
    var bitmap: Bitmap? = null     // 갤러리에서 가져온 사진 bitmap
    var image: File? = null        // 서버에 보낼 image data

    // 앱이 카메라 권한을 가지고 있는지 확인하는 변수 ( 카메라 권한이 없다면 -1 반환 )
    private val permissionCheckCamera by lazy {
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mission_create)
        if (intent.hasExtra("accessToken")) {
            accessToken = intent.getStringExtra("accessToken")
        }
        if (intent.hasExtra("beeId")) {
            beeId = intent.getIntExtra("beeId", 0)
        }
        initButtonListeners()
        initEditTextListeners()
        initEditTextListeners()
        chkPermission()

        mission_create_btn.isEnabled = false
        hard_click_btn.isSelected = false
        normal_click_btn.isSelected = false
        easy_click_btn.isSelected = false

        wrap_click_img_view.visibility = View.VISIBLE
        wrap_load_img_view.visibility = View.INVISIBLE

        reload_img.setColorFilter(Color.parseColor("#AAAAAA"))
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.cancel_btn -> gotoMain(accessToken)
            R.id.mission_create_btn -> missionCreateServer()
            R.id.take_picture_btn -> gotoCamera()
            R.id.get_picture_btn -> gotoGallery()
            R.id.reload_img_btn -> changeWrapView(CLICK_IMAGEVIEW)

            R.id.hard_click_btn -> setMissionDifficulty(2)
            R.id.normal_click_btn -> setMissionDifficulty(1)
            R.id.easy_click_btn -> setMissionDifficulty(0)
        }
    }

    private fun initButtonListeners() {
        cancel_btn.setOnClickListener(this)
        mission_create_btn.setOnClickListener(this)
        take_picture_btn.setOnClickListener(this)
        get_picture_btn.setOnClickListener(this)
        reload_img_btn.setOnClickListener(this)

        hard_click_btn.setOnClickListener(this)
        normal_click_btn.setOnClickListener(this)
        easy_click_btn.setOnClickListener(this)
    }

    private fun initEditTextListeners() {
        description_mission.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(edit: Editable) {

                if (description_mission.text.toString().trim().isEmpty()) {
                    mission_create_btn.setTextColor(Color.parseColor("#CCCCCC"))
                    mission_create_btn.isEnabled = false
                } else if (description_mission.text.toString().trim().length in 2..10) {
                    isActivateButton()
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // 입력이 끝났을 때 -> 다음 넘어가도 됨
                description = description_mission.text.toString()
            }
        })
    }

    private fun isActivateButton() {
        Log.d(TAG, "description: $description")
        Log.d(TAG, "image: $image")
        Log.d(TAG, "difficulty: $difficulty")

        if ((description != "") && (image != null) && (difficulty != -1)) {
            mission_create_btn.setTextColor(Color.parseColor("#F6CD00"))
            mission_create_btn.isEnabled = true
        } else {
            mission_create_btn.setTextColor(Color.parseColor("#CCCCCC"))
            mission_create_btn.isEnabled = false
        }
    }

    private fun missionCreateServer() {
        when {
            difficulty == -1 -> {
                showToast { "난이도 설정해주세요. " }
            }
            image == null -> {
                showToast { "사진을 선택해 주세요." }
            }
            description == "" -> {
                showToast { "미션 타이틀을 등록해주세요. " }
            }
            else -> {
                val imageFile: File = image!!
                val testImage: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "image",
                    imageFile.name,
                    imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                )

                service.missionCreate(accessToken, testImage, beeId, description, type, difficulty)
                    .enqueue(object : Callback<Void> {
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Dlog().d(t.toString())
                        }

                        override fun onResponse(
                            call: Call<Void>,
                            response: Response<Void>
                        ) {
                            when (response.code()) {
                                201 -> {
                                    gotoMain(accessToken)
                                }

                                400 -> {
                                    val jsonObject = JSONObject(response.errorBody()?.string())
                                    val message = jsonObject.getString("message")
                                    showToast { message }
                                    gotoMain(accessToken)
                                }
                                500 -> { //internal server error
                                    val jsonObject = JSONObject(response.errorBody()?.string())
                                    val message = jsonObject.getString("message")
                                    showToast { message }
                                    gotoMain(accessToken)
                                }
                            }
                        }
                    })
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            for (value in grantResults) {
                if (value != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "permission reject")
                }
            }
        }
    }

    private fun gotoGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_FROM_ALBUM)
    }

    private fun gotoCamera() {
        //외장 메모리 (sd card) 연결 여부 확인
        val state: String = Environment.getExternalStorageState()
        if (state != Environment.MEDIA_MOUNTED) {
            Log.d(TAG, "SDcard is not mounted")
            return
        }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        tempFile = createImageFile()
        tempFile?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val photoUri = FileProvider.getUriForFile(
                    this,
                    "${application.packageName}.provider",
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, PICK_FROM_CAMERA)
            } else {
                val photoUri = Uri.fromFile(it)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, PICK_FROM_CAMERA)
            }
        }
    }

    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "morningbees"

        // 외부 앱 전용 저장소
        val path = getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath

        val storageDir = File(path)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        Log.d(TAG, "path 경로: $path")
        try {
            val tempFile = File.createTempFile(imageFileName, ".jpg", storageDir)
            Log.d(TAG, "tempFile: $tempFile")
            return tempFile
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == PICK_FROM_ALBUM) {
            intent?.data?.let { photoUri ->
                val selectedImage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(contentResolver, photoUri)
                    // ImageDecorder.source를 객체로 전달해 Bitmap 생성
                    ImageDecoder.decodeBitmap(source)
                } else {
                    contentResolver.openInputStream(photoUri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                }

                upload_img_view.setImageBitmap(
                    Bitmap.createScaledBitmap(
                        selectedImage,
                        120,
                        120,
                        false
                    )
                )
                upload_img_view.clipToOutline = true

                bitmap = selectedImage!!
                image = File(URIPathHelper().getPath(this, photoUri))
            }
            isActivateButton()
        } else if (requestCode == PICK_FROM_CAMERA) {
            // 카메라에서는 intent, data == null
            val selectedImage = BitmapFactory.decodeFile(tempFile?.absolutePath)
            upload_img_view.setImageBitmap(
                Bitmap.createScaledBitmap(
                    selectedImage,
                    120,
                    120,
                    false
                )
            )
            upload_img_view.clipToOutline = true
            bitmap = selectedImage
            image = tempFile
            isActivateButton()
        }

        if (intent == null) {
            changeWrapView(CLICK_IMAGEVIEW)
        } else {
            changeWrapView(LOAD_IMAGEVIEW)
        }
    }

    private fun changeWrapView(status: Int) { // wrap view change
        Log.d(TAG, "status : $status")
        if (status == CLICK_IMAGEVIEW) {
            wrap_click_img_view.visibility = View.VISIBLE
            wrap_load_img_view.visibility = View.INVISIBLE
            mission_create_btn.setTextColor(Color.parseColor("#F6CD00"))
            mission_create_btn.isEnabled = true
            image = null
        }
        if (status == LOAD_IMAGEVIEW) {
            wrap_load_img_view.visibility = View.VISIBLE
            wrap_click_img_view.visibility = View.INVISIBLE
            mission_create_btn.setTextColor(Color.parseColor("#CCCCCC"))
            mission_create_btn.isEnabled = false
        }
    }

    private fun gotoMain(accessToken: String) {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).putExtra("accessToken", accessToken).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    private fun setMissionDifficulty(mDifficulty: Int) {
        when (mDifficulty) {
            2 -> {
                //상
                hard_click_btn.isSelected = true
                normal_click_btn.isSelected = false
                easy_click_btn.isSelected = false

                hard_price_txt.setTextColor(Color.parseColor("#b29227"))
                hard_txt.setTextColor(Color.parseColor("#444444"))

                normal_price_txt.setTextColor(Color.parseColor("#cccccc"))
                normal_txt.setTextColor(Color.parseColor("#cccccc"))

                easy_price_txt.setTextColor(Color.parseColor("#cccccc"))
                easy_txt.setTextColor(Color.parseColor("#cccccc"))

                hard_click_btn.elevation = 10f
                normal_click_btn.elevation = 0f
                easy_click_btn.elevation = 0f
            }
            1 -> {
                //중
                hard_click_btn.isSelected = false
                normal_click_btn.isSelected = true
                easy_click_btn.isSelected = false

                hard_price_txt.setTextColor(Color.parseColor("#cccccc"))
                hard_txt.setTextColor(Color.parseColor("#cccccc"))

                normal_price_txt.setTextColor(Color.parseColor("#b29227"))
                normal_txt.setTextColor(Color.parseColor("#444444"))

                easy_price_txt.setTextColor(Color.parseColor("#cccccc"))
                easy_txt.setTextColor(Color.parseColor("#cccccc"))

                hard_click_btn.elevation = 0f
                normal_click_btn.elevation = 10f
                easy_click_btn.elevation = 0f
            }
            0 -> {
                //하
                hard_click_btn.isSelected = false
                normal_click_btn.isSelected = false
                easy_click_btn.isSelected = true

                hard_price_txt.setTextColor(Color.parseColor("#cccccc"))
                hard_txt.setTextColor(Color.parseColor("#cccccc"))

                normal_price_txt.setTextColor(Color.parseColor("#cccccc"))
                normal_txt.setTextColor(Color.parseColor("#cccccc"))

                easy_price_txt.setTextColor(Color.parseColor("#b29227"))
                easy_txt.setTextColor(Color.parseColor("#444444"))

                hard_click_btn.elevation = 0f
                normal_click_btn.elevation = 0f
                easy_click_btn.elevation = 10f
            }
        }
        difficulty = mDifficulty
        isActivateButton()
    }

    private fun chkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionCheckCamera == PackageManager.PERMISSION_DENIED) {
                showRequestPermission()
            } else {
                Log.d(TAG, "---- already have permission ----")
            }
        }
    }

    private fun showRequestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            REQUEST_PERMISSION
        )
    }

    companion object {
        private const val REQUEST_PERMISSION = 1000
        private const val PICK_FROM_ALBUM = 1001
        private const val PICK_FROM_CAMERA = 1002
        private const val LOAD_IMAGEVIEW = 1
        private const val CLICK_IMAGEVIEW = 2
        private const val TAG = "MissionCreateActivity"
    }
}
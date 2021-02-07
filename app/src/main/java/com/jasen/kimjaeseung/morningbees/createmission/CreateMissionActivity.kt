package com.jasen.kimjaeseung.morningbees.createmission

import android.Manifest


import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.login.LoginActivity
import com.jasen.kimjaeseung.morningbees.main.MainActivity
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.URIPathHelper
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_create_mission.*

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CreateMissionActivity : AppCompatActivity(), View.OnClickListener {
    val service = MorningBeesService.create()
    var difficulty: Int = -1
    var description: String = ""
    private var beeId: Int = 0
    var tempFile: File? = null     // 카메라로 찍은 사진 File (갤러리에 저장)
    var bitmap: Bitmap? = null     // 갤러리에서 가져온 사진 bitmap
    var image: File? = null        // 서버에 보낼 image data
    var targetDate = ""

    // 앱이 카메라 권한을 가지고 있는지 확인하는 변수 ( 카메라 권한이 없다면 -1 반환 )
    private val permissionCheckCamera by lazy {
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_mission)

        beeId = GlobalApp.prefsBeeInfo.beeId
        targetDate = intent.getStringExtra("targetDate")

        initButtonListeners()
        initEditTextListeners()
        initEditTextListeners()
        chkPermission()

        missionCreateRegisterButton.isEnabled = false
        hardButton.isSelected = false
        normalButton.isSelected = false
        easyButton.isSelected = false

        missionImageUploadWrapLayout.visibility = View.VISIBLE
        missionLoadWrapLayout.visibility = View.INVISIBLE

        reloadMissionIcon.setColorFilter(Color.parseColor("#AAAAAA"))

        initGlobalLayoutListener()
    }

    private fun initGlobalLayoutListener() {
        difficultyBackground.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                difficultyButtonWrapLayout.layoutParams.height = difficultyBackground.height
                difficultyBackground.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.missionCreateCancelButton -> gotoMain()
            R.id.missionCreateRegisterButton -> missionCreateServer()
            R.id.takePictureButton -> gotoCamera()
            R.id.getGalleryButton -> gotoGallery()
            R.id.reloadMissionButton -> changeWrapView(CLICK_IMAGEVIEW)

            R.id.hardButton -> setMissionDifficulty(2)
            R.id.normalButton -> setMissionDifficulty(1)
            R.id.easyButton -> setMissionDifficulty(0)
        }
    }

    private fun initButtonListeners() {
        missionCreateCancelButton.setOnClickListener(this)
        missionCreateRegisterButton.setOnClickListener(this)
        takePictureButton.setOnClickListener(this)
        getGalleryButton.setOnClickListener(this)
        reloadMissionButton.setOnClickListener(this)

        hardButton.setOnClickListener(this)
        normalButton.setOnClickListener(this)
        easyButton.setOnClickListener(this)
    }

    private fun initEditTextListeners() {
        missionDescription.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(edit: Editable) {

                if (missionDescription.text.toString().trim().isEmpty()) {
                    missionCreateRegisterButton.setTextColor(Color.parseColor("#CCCCCC"))
                    missionCreateRegisterButton.isEnabled = false
                } else if (missionDescription.text.toString().trim().length in 2..10) {
                    isActivateButton()
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // 입력이 끝났을 때 -> 다음 넘어가도 됨
                description = missionDescription.text.toString()
            }
        })
    }

    private fun isActivateButton() {
        Log.d(TAG, "description: $description")
        Log.d(TAG, "image: $image")
        Log.d(TAG, "difficulty: $difficulty")

        if ((description != "") && (image != null) && (difficulty != -1)) {
            missionCreateRegisterButton.setTextColor(Color.parseColor("#F6CD00"))
            missionCreateRegisterButton.isEnabled = true
        } else {
            missionCreateRegisterButton.setTextColor(Color.parseColor("#CCCCCC"))
            missionCreateRegisterButton.isEnabled = false
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
                val imageFile = image!!
                val testImage = MultipartBody.Part.createFormData(
                    "image",
                    imageFile.name,
                    imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                )

                service.missionCreate(
                    GlobalApp.prefs.accessToken,
                    testImage,
                    beeId,
                    description,
                    1,
                    difficulty,
                    targetDate
                )
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
                                    gotoMain()
                                }

                                400 -> {
                                    val jsonObject = JSONObject(response.errorBody()?.string())
                                    val message = jsonObject.getString("message")
                                    val code = jsonObject.getInt("code")

                                    if (code == 120) {
                                        val oldAccessToken = GlobalApp.prefs.accessToken
                                        GlobalApp.prefs.requestRenewalApi()
                                        val renewalAccessToken = GlobalApp.prefs.accessToken

                                        if (oldAccessToken == renewalAccessToken) {
                                            showToast { "다시 로그인해주세요." }
                                            gotoLogOut()
                                        } else
                                            missionCreateServer()
                                    } else {
                                        showToast { message }
                                        goFinish()
                                    }
                                }
                                500 -> { //internal server error
                                    val jsonObject = JSONObject(response.errorBody()?.string())
                                    val message = jsonObject.getString("message")
                                    showToast { message }
                                    goFinish()
                                }
                            }
                        }
                    })
            }
        }
    }

    private fun goFinish(){
        finish()
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

    private fun gotoLogOut(){
        startActivity(
            Intent(this, LoginActivity::class.java)
                .putExtra("RequestLogOut", "")
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
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
            Log.d(TAG, "SD card is not mounted")
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
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
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

                loadMissionView.setImageBitmap(
                    Bitmap.createScaledBitmap(
                        selectedImage,
                        120,
                        120,
                        false
                    )
                )
                loadMissionView.clipToOutline = true

                bitmap = selectedImage!!
                image = File(URIPathHelper().getPath(this, photoUri))
            }
            isActivateButton()
        } else if (requestCode == PICK_FROM_CAMERA) {
            // 카메라에서는 intent, data == null
            val selectedImage = BitmapFactory.decodeFile(tempFile?.absolutePath)
            loadMissionView.setImageBitmap(
                Bitmap.createScaledBitmap(
                    selectedImage,
                    120,
                    120,
                    false
                )
            )
            loadMissionView.clipToOutline = true
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
            missionImageUploadWrapLayout.visibility = View.VISIBLE
            missionLoadWrapLayout.visibility = View.INVISIBLE
            missionCreateRegisterButton.setTextColor(Color.parseColor("#F6CD00"))
            missionCreateRegisterButton.isEnabled = true
            image = null
        }
        if (status == LOAD_IMAGEVIEW) {
            missionLoadWrapLayout.visibility = View.VISIBLE
            missionImageUploadWrapLayout.visibility = View.INVISIBLE
            missionCreateRegisterButton.setTextColor(Color.parseColor("#CCCCCC"))
            missionCreateRegisterButton.isEnabled = false
        }
    }

    private fun gotoMain() {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    private fun setMissionDifficulty(mDifficulty: Int) {
        when (mDifficulty) {
            2 -> {
                //상
                hardButton.background =
                    applicationContext.getDrawable(R.drawable.difficulty_selected_button)
                normalButton.background =
                    applicationContext.getDrawable(R.drawable.difficulty_unselected_button)
                easyButton.background =
                    applicationContext.getDrawable(R.drawable.difficulty_unselected_button)

                hardPriceText.setTextColor(Color.parseColor("#b29227"))
                hardText.setTextColor(Color.parseColor("#444444"))

                normalPriceButton.setTextColor(Color.parseColor("#cccccc"))
                normalText.setTextColor(Color.parseColor("#cccccc"))

                easyPriceText.setTextColor(Color.parseColor("#cccccc"))
                easyText.setTextColor(Color.parseColor("#cccccc"))

                hardButton.elevation = 5f
//                hardButton.elevation = 0f
                normalButton.elevation = 0f
                easyButton.elevation = 0f
            }
            1 -> {
                //중
                hardButton.background =
                    applicationContext.getDrawable(R.drawable.difficulty_unselected_button)
                normalButton.background =
                    applicationContext.getDrawable(R.drawable.difficulty_selected_button)
                easyButton.background =
                    applicationContext.getDrawable(R.drawable.difficulty_unselected_button)

                hardPriceText.setTextColor(Color.parseColor("#cccccc"))
                hardText.setTextColor(Color.parseColor("#cccccc"))

                normalPriceButton.setTextColor(Color.parseColor("#b29227"))
                normalText.setTextColor(Color.parseColor("#444444"))

                easyPriceText.setTextColor(Color.parseColor("#cccccc"))
                easyText.setTextColor(Color.parseColor("#cccccc"))

                hardButton.elevation = 0f
                normalButton.elevation = 5f
//                normalButton.elevation = 0f
                easyButton.elevation = 0f
            }
            0 -> {
                //하
                hardButton.background =
                    applicationContext.getDrawable(R.drawable.difficulty_unselected_button)
                normalButton.background =
                    applicationContext.getDrawable(R.drawable.difficulty_unselected_button)
                easyButton.background =
                    applicationContext.getDrawable(R.drawable.difficulty_selected_button)

                hardPriceText.setTextColor(Color.parseColor("#cccccc"))
                hardText.setTextColor(Color.parseColor("#cccccc"))

                normalPriceButton.setTextColor(Color.parseColor("#cccccc"))
                normalText.setTextColor(Color.parseColor("#cccccc"))

                easyPriceText.setTextColor(Color.parseColor("#b29227"))
                easyText.setTextColor(Color.parseColor("#444444"))

                hardButton.elevation = 0f
                normalButton.elevation = 0f
                easyButton.elevation = 5f
//                easyButton.elevation = 0f
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
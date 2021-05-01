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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.ui.signin.LoginActivity
import com.jasen.kimjaeseung.morningbees.ui.main.MainActivity
import com.jasen.kimjaeseung.morningbees.utils.mediascanner.MediaScanner
import com.jasen.kimjaeseung.morningbees.model.error.ErrorResponse
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.utils.Dlog
import com.jasen.kimjaeseung.morningbees.utils.URIPathHelper
import com.jasen.kimjaeseung.morningbees.utils.showToast
import kotlinx.android.synthetic.main.activity_create_mission.*

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class CreateMissionActivity : AppCompatActivity(), View.OnClickListener {

    // Properties

    val service = MorningBeesService.create()
    var difficulty: Int = DIFFICULTY_NONE
    var description: String = ""
    private var beeId: Int = 0
    var mBitmap: Bitmap? = null
    var image: File? = null
    var targetDate = ""
    var currentPhotoPath = ""
    var uriSavedImage = ""

    private val permissionCheckCamera by lazy {
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
    }

    // Life Cycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_mission)

        beeId = GlobalApp.prefsBeeInfo.beeId
        targetDate = intent.getStringExtra("targetDate").toString()

        initButtonListeners()
        initEditTextListeners()
        initEditTextListeners()
        chkPermission()

        missionCreateRegisterButton.isEnabled = false
        setMissionDifficulty(DIFFICULTY_NONE)

        missionImageUploadWrapLayout.visibility = View.VISIBLE
        missionLoadWrapLayout.visibility = View.INVISIBLE
    }

    // Callback Method

    override fun onClick(v: View) {
        when (v.id) {
            R.id.missionCreateCancelButton -> gotoMain()
            R.id.missionCreateRegisterButton -> requestMissionCreateApi()
            R.id.takePictureButton -> dispatchTakePictureIntent()
            R.id.getGalleryButton -> gotoGallery()
            R.id.reloadMissionButton -> changeWrapView(CLICK_IMAGEVIEW)

            R.id.hardButton -> setMissionDifficulty(DIFFICULTY_HARD)
            R.id.normalButton -> setMissionDifficulty(DIFFICULTY_NORMAL)
            R.id.easyButton -> setMissionDifficulty(DIFFICULTY_EASY)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == REQUEST_IMAGE_CAPTURE_FROM_ALBUM) {
            data?.data?.let { photoUri ->
                currentPhotoPath = URIPathHelper().getPath(this, photoUri).toString()

                val selectedImage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(contentResolver, photoUri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    contentResolver.openInputStream(photoUri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                }

                if (selectedImage != null){
                    setPic(currentPhotoPath)
                    loadMissionView.clipToOutline = true
                }
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE_FROM_CAMERA) {
            val mediaScanner = MediaScanner.newInstance(this)
            try {
                mediaScanner.mediaScanning(currentPhotoPath)
                setPic(currentPhotoPath)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(TAG, "Media Scan Error: $e")
            }
        }

        isActivateButton()

        if (mBitmap == null) {
            changeWrapView(CLICK_IMAGEVIEW)
        } else {
            changeWrapView(LOAD_IMAGEVIEW)
        }
    }

    // Init Method

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
                description = missionDescription.text.toString()
            }
        })
    }

    // View Design

    private fun isActivateButton() {
        Log.d(TAG, "description: $description")
        Log.d(TAG, "bitmap: $mBitmap")
        Log.d(TAG, "difficulty: $difficulty")

        if (description == "" || mBitmap == null || difficulty == DIFFICULTY_NONE) {
            missionCreateRegisterButton.setTextColor(Color.parseColor("#CCCCCC"))
            missionCreateRegisterButton.isEnabled = false
        } else {
            missionCreateRegisterButton.setTextColor(Color.parseColor("#F6CD00"))
            missionCreateRegisterButton.isEnabled = true
        }
    }

    private fun setMissionDifficulty(mDifficulty: Int) {
        when (mDifficulty) {
            DIFFICULTY_NONE -> {
                hardSelectedButton.visibility = View.GONE
                hardUnSelectedButton.visibility = View.VISIBLE

                normalSelectedButton.visibility = View.GONE
                normalUnSelectedButton.visibility = View.VISIBLE

                easySelectedButton.visibility = View.GONE
                easyUnSelectedButton.visibility = View.VISIBLE
            }

            DIFFICULTY_HARD -> {
                hardSelectedButton.visibility = View.VISIBLE
                hardUnSelectedButton.visibility = View.GONE

                normalSelectedButton.visibility = View.GONE
                normalUnSelectedButton.visibility = View.VISIBLE

                easySelectedButton.visibility = View.GONE
                easyUnSelectedButton.visibility = View.VISIBLE

            }
            DIFFICULTY_NORMAL -> {
                hardSelectedButton.visibility = View.GONE
                hardUnSelectedButton.visibility = View.VISIBLE

                normalSelectedButton.visibility = View.VISIBLE
                normalUnSelectedButton.visibility = View.GONE

                easySelectedButton.visibility = View.GONE
                easyUnSelectedButton.visibility = View.VISIBLE

            }

            DIFFICULTY_EASY -> {
                hardSelectedButton.visibility = View.GONE
                hardUnSelectedButton.visibility = View.VISIBLE

                normalSelectedButton.visibility = View.GONE
                normalUnSelectedButton.visibility = View.VISIBLE

                easySelectedButton.visibility = View.VISIBLE
                easyUnSelectedButton.visibility = View.GONE
            }
        }
        difficulty = mDifficulty
        isActivateButton()
    }

    private fun setPic(currentPhotoPath: String) {
        val targetW = loadMissionView.width
        val targetH = loadMissionView.height

        val bmOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true

            val photoW = outWidth
            val photoH = outHeight

            val scaleFactor = Math.min(photoW / targetW, photoH / targetH)

            inJustDecodeBounds = false
            inSampleSize = scaleFactor
        }

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
            mBitmap = bitmap
            loadMissionView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 120, 120, false))
        }

        loadMissionView.clipToOutline = true
    }

    private fun changeWrapView(status: Int) { // wrap view change
        if (status == CLICK_IMAGEVIEW) {
            missionImageUploadWrapLayout.visibility = View.VISIBLE
            missionLoadWrapLayout.visibility = View.INVISIBLE
            image = null
            mBitmap = null
        }
        if (status == LOAD_IMAGEVIEW) {
            missionLoadWrapLayout.visibility = View.VISIBLE
            missionImageUploadWrapLayout.visibility = View.INVISIBLE
        }
        isActivateButton()
    }

    // API Request

    private fun requestMissionCreateApi() {
        when {
            difficulty == -1 -> {
                showToast { "난이도 설정해주세요. " }
            }
            mBitmap == null -> {
                showToast { "사진을 선택해 주세요." }
            }
            description == "" -> {
                showToast { "미션 타이틀을 등록해주세요. " }
            }
            else -> {
                val imageFile = File(currentPhotoPath)

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
                                    val converter: Converter<ResponseBody, ErrorResponse> =
                                        MorningBeesService.retrofit.responseBodyConverter<ErrorResponse>(
                                            ErrorResponse::class.java,
                                            ErrorResponse::class.java.annotations
                                        )

                                    val errorResponse = converter.convert(response.errorBody())

                                    if (errorResponse.code == 111 || errorResponse.code == 110 || errorResponse.code == 120) {
                                        val oldAccessToken = GlobalApp.prefs.accessToken
                                        GlobalApp.prefs.requestRenewalApi()
                                        val renewalAccessToken = GlobalApp.prefs.accessToken

                                        if (oldAccessToken == renewalAccessToken) {
                                            showToast { "다시 로그인해주세요." }
                                            gotoLogOut()
                                        } else
                                            requestMissionCreateApi()
                                    } else {
                                        showToast { errorResponse.message }
                                        finish()
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

    // Create Mission with Camera & Gallery

    private fun dispatchTakePictureIntent() {
        val state: String = Environment.getExternalStorageState()
        if (state != Environment.MEDIA_MOUNTED) {
            Log.d(TAG, "SD card is not mounted")
            return
        }

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }

                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.jasen.kimjaeseung.morningbees",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_FROM_CAMERA)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    // Change Activity

    private fun goFinish() {
        finish()
    }

    private fun gotoLogOut() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .putExtra("RequestLogOut", "")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    private fun gotoGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            .putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE_FROM_ALBUM)
    }

    private fun gotoMain() {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    // Check Permission for Camera & Gallery

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
        private const val REQUEST_IMAGE_CAPTURE_FROM_ALBUM = 1001
        private const val REQUEST_IMAGE_CAPTURE_FROM_CAMERA = 1002
        private const val LOAD_IMAGEVIEW = 10
        private const val CLICK_IMAGEVIEW = 11

        private const val DIFFICULTY_NONE = 3
        private const val DIFFICULTY_HARD = 2
        private const val DIFFICULTY_NORMAL = 1
        private const val DIFFICULTY_EASY = 0

        private const val TAG = "MissionCreateActivity"
    }
}
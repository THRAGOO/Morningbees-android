package com.jasen.kimjaeseung.morningbees.participatemission

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.model.error.ErrorResponse
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_participate_upload_mission.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Response
import java.io.File

class ParticipateMissionActivity : AppCompatActivity(), View.OnClickListener {

    // Properties

    private val service = MorningBeesService.create()
    var accessToken = ""
    var beeId = -1
    var imageFile: File? = null
    var difficulty = -1
    var targetDate = ""
    private var currentPhotoPath = ""

    // Life Cycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_participate_upload_mission)

        accessToken = GlobalApp.prefs.accessToken
        beeId = GlobalApp.prefsBeeInfo.beeId
        difficulty = intent.getIntExtra("difficulty", -1)
        targetDate = intent.getStringExtra("targetDate")!!

        currentPhotoPath = intent.getStringExtra("photoPath").toString()
        val state = intent.getIntExtra("state", 0)
        initButtonListeners()

        pc_upload_img.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                setPic(pc_upload_img.width, pc_upload_img.height)
                pc_upload_img.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    // Callback Method

    override fun onClick(v: View) {
        when (v.id) {
            R.id.cancelMissionUploadButton -> gotoMain()
            R.id.reloadMissionParticipateButton -> gotoParticipateDialog()
            R.id.uploadMissionParticipateButton -> requestMissionCreateApi()
        }
    }

    // Init Method

    private fun initButtonListeners() {
        cancelMissionUploadButton.setOnClickListener(this)
        reloadMissionParticipateButton.setOnClickListener(this)
        uploadMissionParticipateButton.setOnClickListener(this)
    }

    // View Design

    private fun setPic(targetW: Int, targetH: Int) {
        val bmOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            val photoW = outWidth
            val photoH = outHeight

            val scaleFactor = Math.min(photoW / targetW, photoH / targetH)

            inJustDecodeBounds = false
            inSampleSize = scaleFactor
        }

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
            pc_upload_img.setImageBitmap(bitmap)
        }
        pc_upload_img.clipToOutline = true
    }

    // API Request

    private fun requestMissionCreateApi() {
        if (currentPhotoPath == "") {
            showToast { "사진을 선택해 주세요." }
        } else {
            val image = File(currentPhotoPath)

            val testImage: MultipartBody.Part = MultipartBody.Part.createFormData(
                "image",
                image.name,
                image.asRequestBody("image/*".toMediaTypeOrNull())
            )
            // 이 부분을 고쳐야할 듯.. 자꾸 화질이 낮아진당..

            service.missionCreate(accessToken, testImage, beeId, "", 2, difficulty, targetDate)
                .enqueue(object : retrofit2.Callback<Void> {
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
                                Log.d(TAG, "mission participate success")
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
                                        gotoMain()
                                    } else
                                        requestMissionCreateApi()
                                } else {
                                    showToast { errorResponse.message }
                                    finish()
                                }
                            }

                            500 -> {
                                val jsonObject = JSONObject(response.errorBody()!!.string())
                                val timestamp = jsonObject.getString("timestamp")
                                val status = jsonObject.getString("status")
                                val message = jsonObject.getString("message")
                                val code = jsonObject.getInt("code")
                                gotoMain()
                                showToast { message }
                            }
                        }
                    }
                })
        }
    }

    // Change Activity

    private fun gotoParticipateDialog() {
        val result = Intent()
        setResult(RELOAD, result)
        finish()
    }

    private fun gotoMain() {
        val result = Intent()
        setResult(FINISH, result)
        finish()
    }

    companion object {
        private const val TAG = "MissionParticipate"
        private const val RELOAD = 120
        private const val FINISH = 121

        private const val PICK_FROM_ALBUM = 1001
        private const val PICK_FROM_CAMERA = 1002
    }

}
package com.jasen.kimjaeseung.morningbees.participatemission

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
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
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class ParticipateMissionActivity : AppCompatActivity(), View.OnClickListener {
    private val service = MorningBeesService.create()

    // MissionCreate API
    var accessToken = ""
    var beeId = -1
    var imageFile: File? = null
    var difficulty = -1
    var targetDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_participate_upload_mission)

        accessToken = GlobalApp.prefs.accessToken
        beeId = GlobalApp.prefsBeeInfo.beeId
        difficulty = intent.getIntExtra("difficulty", -1)
        targetDate = intent.getStringExtra("targetDate")

//        val byteArray: ByteArray = intent.getByteArrayExtra("missionImage")
        val uri = intent.getStringExtra("missionImage")
        val state = intent.getIntExtra("state", 0)

        if (state == PICK_FROM_ALBUM) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, Uri.parse(uri))
                val bitmap = ImageDecoder.decodeBitmap(source)
                imageFile = bitmapToFile(bitmap)
                pc_upload_img.setImageBitmap(bitmap)
            } else {
                val bitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.parse(uri))
                imageFile = bitmapToFile(bitmap)
                pc_upload_img.setImageBitmap(bitmap)
            }
        } else if (state == PICK_FROM_CAMERA) {
            val selectedImage = BitmapFactory.decodeFile(uri)
            imageFile = bitmapToFile(selectedImage)
            pc_upload_img.setImageBitmap(selectedImage)
        }

        pc_reload_img.setColorFilter(Color.parseColor("#aaaaaa"))
        initButtonListeners()
    }

    private fun initButtonListeners() {
        cancel_participate_upload_btn.setOnClickListener(this)
        pc_reload_img_btn.setOnClickListener(this)
        share_participate_upload_btn.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.cancel_participate_upload_btn -> gotoMain()
            R.id.pc_reload_img_btn -> gotoParticipateDialog()
            R.id.share_participate_upload_btn -> requestMissionCreateApi()
        }
    }

    private fun bitmapToFile(bitmap: Bitmap): File {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            //compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        Log.d(TAG, "Uri: ${Uri.parse(file.absolutePath)}")
        Log.d(TAG, "file absolutePath: ${file.absolutePath}")
        Log.d(TAG, "file: $file")

        return file
    }

    private fun requestMissionCreateApi() {
        if (imageFile == null) {
            showToast { "사진을 선택해 주세요." }
        } else {
            val image: File = imageFile!!
            val testImage: MultipartBody.Part = MultipartBody.Part.createFormData(
                "image",
                image.name,
                image.asRequestBody("image/*".toMediaTypeOrNull())
            )

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

                                if(errorResponse.code == 111 || errorResponse.code == 110 || errorResponse.code == 120){
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

                            500 -> { //internal server error
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
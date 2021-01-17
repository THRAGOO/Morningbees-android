package com.jasen.kimjaeseung.morningbees.missionparticipate

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.missioncreate.MissionCreateActivity
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_mission_participate_upload.*
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class MissionParticipateActivity : AppCompatActivity(), View.OnClickListener {
    private val service =  MorningBeesService.create()

    // MissionCreate API
    var accessToken : String = ""
    var beeId : Int = -1
    var imageFile : File? = null
    var difficulty : Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mission_participate_upload)

        val byteArray : ByteArray = intent.getByteArrayExtra("missionImage")
        accessToken = GlobalApp.prefs.accessToken
        beeId = intent.getIntExtra("beeId", 0)
        difficulty = intent.getIntExtra("difficulty", -1)
        val bitmap : Bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        imageFile = bitmapToFile(bitmap)
        pc_upload_img.setImageBitmap(bitmap)

        initButtonListeners()
    }

    private fun initButtonListeners(){
        cancel_participate_upload_btn.setOnClickListener(this)
        pc_reload_img_btn.setOnClickListener(this)
        share_participate_upload_btn.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val i = v.id
        when(i){
            R.id.cancel_participate_upload_btn -> gotoMain()
            R.id.pc_reload_img_btn -> gotoParticipateDialog()
            R.id.share_participate_upload_btn -> missionCreateServer()
        }
    }

    private fun bitmapToFile(bitmap: Bitmap) : File {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try{
            //compress the bitmap and save in jpg format
            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException){
            e.printStackTrace()
        }

        Log.d(TAG, "Uri: ${Uri.parse(file.absolutePath)}")
        Log.d(TAG, "file absolutePath: ${file.absolutePath}")
        Log.d(TAG, "file: $file")

        return file
    }

    private fun missionCreateServer(){
        if(imageFile == null){
            showToast { "사진을 선택해 주세요." }
        } else {
            val image : File = imageFile!!
            val testImage : MultipartBody.Part = MultipartBody.Part.createFormData("image", image.name, image.asRequestBody("image/*".toMediaTypeOrNull()))

            service.missionCreate(accessToken, testImage, beeId, "", 2, difficulty)
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
                                val jsonObject = JSONObject(response.errorBody()!!.string())
                                val timestamp = jsonObject.getString("timestamp")
                                val status = jsonObject.getString("status")
                                val message = jsonObject.getString("message")
                                val code = jsonObject.getInt("code")
                                showToast { message }
                            }
                            500 -> { //internal server error
                                val jsonObject = JSONObject(response.errorBody()!!.string())
                                val timestamp = jsonObject.getString("timestamp")
                                val status = jsonObject.getString("status")
                                val message = jsonObject.getString("message")
                                val code = jsonObject.getInt("code")
                                showToast { message }
                            }
                        }
                    }
                })
        }

    }

    private fun gotoParticipateDialog(){
        val result = Intent()
        setResult(RELOAD, result)
        finish()
    }

    private fun gotoMain(){
        val result = Intent()
        setResult(FINISH, result)
        finish()
    }


    companion object {
        private const val TAG = "MissionParticipate"
        private const val RELOAD = 120
        private const val FINISH = 121
    }

}
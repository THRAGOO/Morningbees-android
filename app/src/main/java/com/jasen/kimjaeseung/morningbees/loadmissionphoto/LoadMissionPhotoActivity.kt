package com.jasen.kimjaeseung.morningbees.loadmissionphoto

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.login.LoginActivity
import com.jasen.kimjaeseung.morningbees.model.error.ErrorResponse
import com.jasen.kimjaeseung.morningbees.model.missioninfo.Mission
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_create_step3.*
import kotlinx.android.synthetic.main.activity_load_mission_photo.*
import kotlinx.android.synthetic.main.item_mission_photo.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response

class LoadMissionPhotoActivity : AppCompatActivity(), View.OnClickListener {
    val service = MorningBeesService.create()
    var targetDate = ""
    var missionList = mutableListOf<Mission>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_mission_photo)
        targetDate = intent.getStringExtra("targetDate")!!

        initButtonListener()
//        initImageView()
        requestMissionAPI()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.gotoMainFromMissionPhotoButton -> finish()
        }
    }

    private fun initImageView(){
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display!!.getRealMetrics(displayMetrics)
        } else {
            windowManager.defaultDisplay.getMetrics(displayMetrics)
        }

        Log.d("activity", "displayMetrics.widthPixels ${displayMetrics.widthPixels}")
        itemMissionPhotoImage.layoutParams.width = displayMetrics.widthPixels
        itemMissionPhotoImage.layoutParams.height = displayMetrics.widthPixels / 3 * 4
    }

    private fun requestMissionAPI() {
        service.missionInfo(
            GlobalApp.prefs.accessToken,
            targetDate, GlobalApp.prefsBeeInfo.beeId
        )
            .enqueue(object : Callback<List<Mission>> {
                override fun onFailure(call: Call<List<Mission>>, t: Throwable) {
                    Dlog().d(t.toString())
                }

                override fun onResponse(
                    call: Call<List<Mission>>,
                    response: Response<List<Mission>>
                ) {
                    when (response.code()) {
                        200 -> {
                            val missionInfoResponse = response.body()

                            if(missionInfoResponse != null){
                                for(i in 0..missionInfoResponse.size-1){
                                    val missionInfo = missionInfoResponse[i]
                                    if(missionInfo.type == 2){
                                        missionList.add(missionInfo)
                                    }
                                }
                            }
                            initRecyclerView()
                        }

                        400 -> {
                            val converter: Converter<ResponseBody, ErrorResponse> =
                                MorningBeesService.retrofit.responseBodyConverter<ErrorResponse>(
                                    ErrorResponse::class.java,
                                    ErrorResponse::class.java.annotations
                                )

                            val errorResponse = converter.convert(response.errorBody())

                            when (errorResponse.code) {
                                110 -> {
                                    val oldAccessToken = GlobalApp.prefs.accessToken
                                    GlobalApp.prefs.requestRenewalApi()
                                    val renewalAccessToken = GlobalApp.prefs.accessToken

                                    if (oldAccessToken == renewalAccessToken) {
                                        showToast { "다시 로그인해주세요." }
                                        gotoLogOut()
                                    } else
                                        requestMissionAPI()
                                }

                                else -> {
                                    showToast { errorResponse.message }
                                    finish()
                                }
                            }
                        }

                        500 -> {
                            val jsonObject = JSONObject(response.errorBody()?.string())
                            val message = jsonObject.getString("message")
                            showToast { message }
                        }
                    }
                }
            })
    }

    private fun initButtonListener() {
        gotoMainFromMissionPhotoButton.setOnClickListener(this)
    }

    private fun initRecyclerView() {
        missionPhotoRecyclerView.adapter = LoadMissionPhotoAdapter(missionList)
        missionPhotoRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun gotoLogOut() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .putExtra("RequestLogOut", "")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)        )
    }
}
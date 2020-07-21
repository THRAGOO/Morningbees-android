package com.jasen.kimjaeseung.morningbees.main

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.beforejoin.model.MeResponse
import com.jasen.kimjaeseung.morningbees.login.LoginActivity
import com.jasen.kimjaeseung.morningbees.main.model.BeeInfoResponse
import com.jasen.kimjaeseung.morningbees.main.model.MissionInfoRequest
import com.jasen.kimjaeseung.morningbees.main.model.MissionInfoResponse
import com.jasen.kimjaeseung.morningbees.missioncreate.MissionCreateActivity
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URI
import java.net.URL
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var accessToken : String
    private val service =  MorningBeesService.create()

    var isManager : Boolean = false
    var title : String = ""
    var missionTime : String = ""
    var totalPay : Int = 0
    var todayUser : String = ""

    var missionImgURL : URL? = null
    var missionDesc : String = ""

    var missionImgURLList  = ArrayList<String>() // <URL> 로 해야됨

    private lateinit var adapter: MainRecyclerViewAdapter
    private lateinit var layoutManager: LinearLayoutManager

    var mDate : Date = Date()
    var targetDate : String = SimpleDateFormat("yyyy-MM-dd").format(mDate)

    var beeId : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(intent.hasExtra("accessToken")) {
            accessToken = intent.getStringExtra("accessToken")
        }
        initRecyclerView()
        initButtonListeners()
    }

    override fun onStart() {
        super.onStart()
        beeInfoServer(accessToken)
        missionInfoServer(accessToken)
        setImgView()
        meServer(accessToken)
    }

    override fun onClick(v: View) {
        val i = v.id
        when(i){
            R.id.go_mission_create_btn -> gotoMissionCreate()
            R.id.go_mission_participate_btn -> goMissionParticipate()
            R.id.calendar_btn -> changeTargetDate()
        }
    }

    private fun changeTargetDate(){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, month, day ->
            today_date.setText(""+year+"-"+month+"-"+day)
        }, year, month, day)
        dpd.show()
    }

    //startactivity stack check 하기
    private fun beeInfoServer(accessToken : String){
        service.beeInfo(accessToken).enqueue(object : Callback<BeeInfoResponse> {
            override fun onFailure(call: Call<BeeInfoResponse>, t: Throwable) {
                Log.d(TAG, "bee info server fail")
                Dlog().d(t.toString())
            }

            override fun onResponse(call: Call<BeeInfoResponse>, response: Response<BeeInfoResponse>) {
                Log.d(TAG, "bee info server success")
                val i = response.code()
                Dlog().d(response.code().toString())
                when(i){
                    200 ->{
                        val beeInfoResponse : BeeInfoResponse? = response.body()
                        var mAccessToken : String = ""

                        Log.d(TAG, "isManager: ${beeInfoResponse?.isManager}")
                        if (beeInfoResponse?.isManager!!){
                            //manager
                            mAccessToken = beeInfoResponse.accessToken
                            title = beeInfoResponse.title
                            missionTime = beeInfoResponse.missionTime
                            totalPay = beeInfoResponse.totalPay
                            todayUser = beeInfoResponse.todayUser
                            Log.d(TAG, "title: $title")
                        }
                        else {
                            title = beeInfoResponse.title
                            missionTime = beeInfoResponse.missionTime
                            totalPay = beeInfoResponse.totalPay
                            todayUser = beeInfoResponse.todayUser
                            Log.d(TAG, "title: $title")
                        }
                    }

                    400 -> {
                        val jsonObject = JSONObject(response.errorBody()!!.string())
                        val timestamp = jsonObject.getString("timestamp")
                        val status = jsonObject.getString("status")
                        val message = jsonObject.getString("message")
                        val code = jsonObject.getInt("code")
                        showToast { message }
                    }

                    500 -> {
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

    fun missionInfoServer(accessToken: String){
        val missionInfoRequest = MissionInfoRequest(targetDate)
        service.missionInfo(accessToken, targetDate).enqueue(object : Callback<MissionInfoResponse>{
            override fun onFailure(call: Call<MissionInfoResponse>, t: Throwable) {
                Dlog().d(t.toString())
            }

            override fun onResponse(
                call: Call<MissionInfoResponse>, response: Response<MissionInfoResponse>) {
                when (response.code()){
                    200 -> {
                        val jsonObject = JSONObject()
                        val jsonArray : JSONArray = jsonObject.getJSONArray("missions")

                        for(i in 0 until jsonArray.length()){
                            val mission = jsonArray.getJSONObject(i)

                            if (i == 0){
                                //missionImgURL = mission.getURL("image_url")
                                // 여기 missionImgURL 받아오는 거 해야함

                                missionDesc = mission.getString("desc")
                            }
                            else {
                                missionImgURLList.add(mission.getString("image_url"))
                            }
                        }
                    }

                    400 -> {
                        val jsonObject = JSONObject(response.errorBody()!!.string())
                        val timestamp = jsonObject.getString("timestamp")
                        val status = jsonObject.getString("status")
                        val message = jsonObject.getString("message")
                        val code = jsonObject.getInt("code")
                        showToast { message }
                    }

                    500 -> {
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

    private fun meServer(accessToken : String){
        var alreadyJoin : Boolean
        var nickname : String = ""

        service.me(accessToken)
            .enqueue(object : Callback<MeResponse>{
                override fun onResponse(call: Call<MeResponse>, response: Response<MeResponse>) {
                    val i = response.code()

                    when(i){
                        200 ->{
                            val meResponse : MeResponse = response.body()!!
                            alreadyJoin = meResponse.alreadyJoin
                            nickname = meResponse.nickname
                            beeId = meResponse.beeId
                        }

                        400 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val timestamp = jsonObject.getString("timestamp")
                            val status = jsonObject.getString("status")
                            val message = jsonObject.getString("message")
                            val code = jsonObject.getInt("code")

                            showToast { message }
                        }

                        500 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val timestamp = jsonObject.getString("timestamp")
                            val status = jsonObject.getString("status")
                            val message = jsonObject.getString("message")
                            val code = jsonObject.getInt("code")

                            showToast { message }
                        }
                    }
                }

                override fun onFailure(call: Call<MeResponse>, t: Throwable) {
                    Dlog().d(t.toString())
                }
            })
    }

    private fun setImgView(){
        if(missionImgURL == null){
            //today_mission_image.setImageBitmap()
            wrap_upload_mission_view.visibility = View.INVISIBLE
            wrap_not_upload_mission_view.visibility = View.VISIBLE
        }
        else{
            wrap_upload_mission_view.visibility = View.VISIBLE
            wrap_not_upload_mission_view.visibility = View.INVISIBLE

            // uri -> bitmap
            uriToBitmap(missionImgURL!!)
            today_mission_text2.text = title
            mission_time_txt.text = missionTime
        }
    }

    private fun uriToBitmap(url: URL){

    }

    fun initButtonListeners(){
        go_mission_create_btn.setOnClickListener(this)
        go_mission_participate_btn.setOnClickListener(this)
        calendar_btn.setOnClickListener(this) 

    }

    fun gotoMissionCreate(){
        if(beeId == 0){
            showToast{"가입된 bee가 없습니다. "}
        }
        else{
            val nextIntent = Intent(this, MissionCreateActivity::class.java)
            nextIntent.putExtra("accessToken", accessToken)
            nextIntent.putExtra("beeId", beeId)
            nextIntent.putExtra("type", 1)
            startActivity(nextIntent)
        }
    }

    fun goMissionParticipate(){
        if(beeId == 0){
            showToast{"가입된 bee가 없습니다. "}
        }
        else{
            val nextIntent = Intent(this, MissionCreateActivity::class.java)
            nextIntent.putExtra("accessToken", accessToken)
            nextIntent.putExtra("beeId", beeId)
            nextIntent.putExtra("type", 0)
            startActivity(nextIntent)
        }
    }

    private fun initRecyclerView(){
        adapter = MainRecyclerViewAdapter(this)
        main_recycler_view.adapter = adapter
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)
        main_recycler_view.layoutManager = layoutManager
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

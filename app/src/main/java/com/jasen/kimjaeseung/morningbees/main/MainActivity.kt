package com.jasen.kimjaeseung.morningbees.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.beforejoin.model.MeResponse
import com.jasen.kimjaeseung.morningbees.calendar.CalendarDialog
import com.jasen.kimjaeseung.morningbees.createlink.CreateLinkActivity
import com.jasen.kimjaeseung.morningbees.main.model.*
import com.jasen.kimjaeseung.morningbees.missioncreate.MissionCreateActivity
import com.jasen.kimjaeseung.morningbees.missionparticipate.MissionParticipateActivity
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.net.URL

class MainActivity : AppCompatActivity(), View.OnClickListener{
    private val service =  MorningBeesService.create()
    private lateinit var accessToken : String
    var beeId : Int = -1
    private lateinit var targetDate : String
    private lateinit var todayDate : String

    //meServer response
    var alreadyJoin : Boolean = false
    var myNickname : String = ""

    //mainServer response
    lateinit var missionImgURL : String
    var todayBee : String? = null

    //recyclerView
    private lateinit var adapter: MainRecyclerViewAdapter
    private lateinit var layoutManager: LinearLayoutManager

    //today mission imgview state
    val EXIST_MISSION = 1
    val NOT_EXIST_MISSION = 2

    var urlList = mutableListOf<URL?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(intent.hasExtra("accessToken")) {
            accessToken = intent.getStringExtra("accessToken")
        }

        initRecyclerView(urlList)
        initButtonListeners()

        setTargetDate()
        meServer(accessToken)
        scrollview.scrollTo(0, scroll.top)
    }

    private fun setTargetDate(){
        val curDate = Date()
        val simpleDate = SimpleDateFormat("yyyy-MM-dd").format(curDate)
        targetDate = simpleDate
        today_date.text = targetDate
        todayDate = targetDate
        Log.d(TAG, "targetDate: $targetDate")
    }

    override fun onClick(v: View) {
        val i = v.id
        when(i){
            R.id.go_mission_create_btn -> gotoMissionCreate()
            R.id.go_mission_participate_btn -> goMissionParticipate()
            R.id.calendar_btn -> changeTargetDate()
            R.id.go_createlink_btn -> goCreateLink()
        }
    }

    fun goCreateLink(){
        val intent = Intent(this, CreateLinkActivity::class.java)
        intent.putExtra("beeid", beeId)
        startActivity(intent)
    }

    private fun changeTargetDate(){
        val dialogFragment = CalendarDialog()
        dialogFragment.show(supportFragmentManager, "signature")

        dialogFragment.setDialogResult(object : CalendarDialog.OnMyDialogResult {
            override fun finish(str: String) {
                targetDate = str
                today_date.text = targetDate
                mainServer(accessToken, beeId)
            }
        })
    }

    private fun mainServer(accessToken: String, beeId : Int){
        service.main(accessToken, targetDate, beeId)
            .enqueue(object : Callback<MainResponse>{
                override fun onFailure(call: Call<MainResponse>, t: Throwable) {
                    Dlog().d(t.toString())
                    Log.d(TAG, "onFailure()")
                }

                override fun onResponse(call: Call<MainResponse>, response: Response<MainResponse>) {
                    when(response.code()){
                        200-> {
                            try {
                                val responseStr = response.body().toString()
                                Log.d(TAG, "responseStr: $responseStr")

                                val mainResponse = MainResponse(
                                    response.body()?.missions,
                                    response.body()?.beeInfos
                                )

                                //null check
                                val missionsResponse = mainResponse.missions
                                Log.d(TAG, "missions:${response.body()?.missions}")
                                val beeInfos = mainResponse.beeInfos
                                Log.d(TAG, "beeInfos:${response.body()?.beeInfos}")

                                if (missionsResponse == null) {
                                    // 오늘의 미션이 설정되지 않았음
                                    setImgView(NOT_EXIST_MISSION)
                                } else {
                                    for (i in 0 until (missionsResponse.size() - 1)) {
                                        //for (i in 0 until (missionsResponse.size()-1)){
                                        val item = missionsResponse.get(i)
                                        val tempCreatedAt =
                                            item.asJsonObject.get("createdAt").asString
                                        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                                        val date = dateFormat.parse(tempCreatedAt)
                                        val createdAt = dateFormat.format(date)

                                        val type = item.asJsonObject.get("type").asInt

                                        Log.d(TAG, "createdAt: $createdAt")
                                        Log.d(TAG, "targetDate: $targetDate")

                                        if (createdAt == targetDate && type == 1) { // test 끝난후에 ||를 &&로 바꾸기
                                            Log.d(TAG, "show mission guide img")
                                            val missionId = item.asJsonObject.get("missionId").asInt
                                            val imageUrl =
                                                item.asJsonObject.get("imageUrl").asString
                                            val nickname =
                                                item.asJsonObject.get("nickname").asString
                                            val difficulty =
                                                item.asJsonObject.get("difficulty").asInt
                                            val createdAt =
                                                item.asJsonObject.get("createdAt").asString
                                            val agreeCount =
                                                item.asJsonObject.get("agreeCount").asInt
                                            val disagreeCount =
                                                item.asJsonObject.get("disagreeCount").asInt

                                            //apply activity_main.xml
                                            Glide.with(this@MainActivity)
                                                .load(imageUrl)
                                                .centerCrop()
                                                .apply(RequestOptions.bitmapTransform(RoundedCorners(30)))
                                                .override(312, 400)
                                                .error(R.drawable.not_upload_mission_img_view)
                                                .into(today_mission_image)


                                            setImgView(EXIST_MISSION)
                                            missionImgURL = imageUrl

                                            Log.d(TAG, "imageUrl: $imageUrl")
                                        }

                                        if (type == 2) {
                                            // 미션 성공 사진
                                            Log.d(TAG, "show mission success img")
                                            val missionId = item.asJsonObject.get("missionId").asInt
                                            val imageUrl =
                                                item.asJsonObject.get("imageUrl").asString
                                            val nickname =
                                                item.asJsonObject.get("nickname").asString
                                            val difficulty =
                                                item.asJsonObject.get("difficulty").asInt
                                            val createdAt =
                                                item.asJsonObject.get("createdAt").asString
                                            val agreeCount =
                                                item.asJsonObject.get("agreeCount").asInt
                                            val disagreeCount =
                                                item.asJsonObject.get("disagreeCount").asInt

                                            val url = URL(imageUrl)
                                            urlList.add(url)
                                        }
                                    }
                                }
                                //apply recycler view
                                // 여기서 recyclerview adapter에 아까 만든 url list 객체 던지기 + missionCnt 변수
                               //initRecyclerView(urlList)
                                if (beeInfos == null) {
                                    // bee가 만들어지지 않았음
                                    setImgView(NOT_EXIST_MISSION)
                                } else {

                                    val totalPenalty = beeInfos.get("totalPenalty").asInt
                                    val memberCounts = beeInfos.get("memberCounts").asInt

                                    val todayQuestioner = beeInfos.get("todayQuestioner")
                                    val todayNickname =
                                        todayQuestioner.asJsonObject.get("nickname").toString()
                                    val todayProfileImage= todayQuestioner.asJsonObject.get("profileImage").toString()

                                    val todayDifficulty =
                                        beeInfos.get("todayDifficulty").asInt // nullable
                                    val startTime = beeInfos.get("startTime").asInt
                                    val endTime = beeInfos.get("endTime").asInt
                                    val title = beeInfos.get("title").asString

                                    val nextQuestioner = beeInfos.get("nextQuestioner")
                                    val nextNickname =
                                        nextQuestioner.asJsonObject.get("nickname").toString()
                                    val nextProfileImage= todayQuestioner.asJsonObject.get("profileImage").toString()

                                    //apply activity_main.xm
                                    if (todayDifficulty == null) {
                                        wrap_define_difficulty_btn.visibility = View.INVISIBLE
                                        wrap_undefine_difficulty_btn.visibility = View.VISIBLE

                                    } else {
                                        wrap_define_difficulty_btn.visibility = View.VISIBLE
                                        wrap_undefine_difficulty_btn.visibility = View.INVISIBLE
                                        setDifficulty(todayDifficulty)
                                    }

                                    if (startTime == null || endTime == null) {
                                        wrap_defined_time_btn.visibility = View.INVISIBLE
                                        wrap_undefined_time_btn.visibility = View.VISIBLE
                                    } else {
                                        wrap_defined_time_btn.visibility = View.VISIBLE
                                        wrap_undefined_time_btn.visibility = View.INVISIBLE
                                        mission_start_time_txt.text = startTime.toString()
                                        mission_end_time_txt.text = endTime.toString()
                                    }

                                    today_mission_text2.text = title
                                    total_member_number.text = memberCounts.toString()
                                    total_pay.text = " " + totalPenalty + "원"
                                    todayBee = todayNickname
                                    setImgView(EXIST_MISSION)

                                    Glide.with(this@MainActivity)
                                        .load(todayProfileImage)
                                        .centerCrop()
                                        .apply(RequestOptions.bitmapTransform(RoundedCorners(100)))
                                        .override(45, 45)
                                        .error(R.drawable.not_upload_mission_img_view)
                                        .into(today_bee_img)



                                    Glide.with(this@MainActivity)
                                        .load(nextProfileImage)
                                        .centerCrop()
                                        .apply(RequestOptions.bitmapTransform(RoundedCorners(100)))
                                        .override(30, 30)
                                        .error(R.drawable.not_upload_mission_img_view)
                                        .into(next_bee_img)

                                    today_user_nickname.text = todayBee
                                }
                            }catch (e: JSONException){}
                        }

                        400->{
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val timestamp = jsonObject.getString("timestamp")
                            val status = jsonObject.getString("status")
                            val message = jsonObject.getString("message")
                            val code = jsonObject.getInt("code")
                            showToast { message }
                            finish()
                        }
                        500->{
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

    fun setDifficulty(difficulty : Int){
        when (difficulty) {
            0 -> {
                mission_difficulty_img.setImageDrawable(getDrawable(R.drawable.low_level))
            }
            1 -> {
                mission_difficulty_img.setImageDrawable(getDrawable(R.drawable.middle_level))
            }
            2 -> {
                mission_difficulty_img.setImageDrawable(getDrawable(R.drawable.high_level))
            }
        }
    }


    private fun meServer(accessToken : String) {
        service.me(accessToken)
            .enqueue(object : Callback<MeResponse>{
                override fun onResponse(call: Call<MeResponse>, response: Response<MeResponse>) {
                    val i = response.code()

                    when(i){
                        200 ->{
                            val meResponse : MeResponse = response.body()!!
                            myNickname = meResponse.nickname
                            alreadyJoin = meResponse.alreadyJoin
                            beeId = meResponse.beeId

                            mainServer(accessToken, beeId)
                        }

                        400 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val timestamp = jsonObject.getString("timestamp")
                            val status = jsonObject.getString("status")
                            val message = jsonObject.getString("message")
                            val code = jsonObject.getInt("code")

                            showToast { message }
                            finish()
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


    private fun setImgView(state : Int){
        if(state == EXIST_MISSION ){
            Log.d(TAG, "state: EXIST_MISSION")
            if(myNickname == todayBee){
                wrap_upload_mission_view.visibility = View.VISIBLE
                wrap_not_upload_mission_view.visibility = View.INVISIBLE
                go_mission_create_btn.visibility = View.VISIBLE
            }
            else{
                wrap_upload_mission_view.visibility = View.VISIBLE
                wrap_not_upload_mission_view.visibility = View.INVISIBLE
                go_mission_create_btn.visibility = View.INVISIBLE
            }
        }
        else if (state == NOT_EXIST_MISSION){
            Log.d(TAG, "state: NOT_EXIST_MISSION")
            if(myNickname == todayBee){
                wrap_upload_mission_view.visibility = View.INVISIBLE
                wrap_not_upload_mission_view.visibility = View.VISIBLE
                go_mission_create_btn.visibility = View.VISIBLE
                go_mission_participate_btn.visibility = View.INVISIBLE
            }
            else{
                wrap_upload_mission_view.visibility = View.VISIBLE
                wrap_not_upload_mission_view.visibility = View.INVISIBLE
                // 임의로 mission create  테스트할 수 있게 !
                go_mission_create_btn.visibility = View.INVISIBLE
                go_mission_participate_btn.visibility = View.VISIBLE

                /*
                // 제대로 구현했을 시, 이렇게 되어야함 !
                go_mission_create_btn.visibility = View.INVISIBLE
                go_mission_participate_btn.visibility = View.VISIBLE
                 */
            }

        }
    }

    private fun initButtonListeners(){
        go_mission_create_btn.setOnClickListener(this)
        go_mission_participate_btn.setOnClickListener(this)
        calendar_btn.setOnClickListener(this)
        go_createlink_btn.setOnClickListener(this)
    }

    private fun gotoMissionCreate(){
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

    private fun goMissionParticipate(){
        if(beeId == 0){
            showToast{"가입된 bee가 없습니다. "}
        }
        else{
            val bottomSheetDialog = BottomSheetDialog(
                this, R.style.BottomSheetDialogTheme
            )

            val bottomSheetView = LayoutInflater.from(applicationContext)
                .inflate(R.layout.activity_mission_participate,
                    findViewById(R.id.layout_mission_participate))

            bottomSheetDialog.setContentView(bottomSheetView)
            bottomSheetDialog.show()
            /*
            val nextIntent = Intent(this, MissionParticipateActivity::class.java)
            nextIntent.putExtra("accessToken", accessToken)
            nextIntent.putExtra("beeId", beeId)
            nextIntent.putExtra("type", 0)
            startActivity(nextIntent)*/
        }
    }

    private fun initRecyclerView(list : MutableList<URL?>){
        adapter = MainRecyclerViewAdapter(list, this)
        main_recycler_view.adapter = adapter
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)
        main_recycler_view.layoutManager = layoutManager
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

package com.jasen.kimjaeseung.morningbees.createbee

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.createbee.model.CreateBeeRequest
import com.jasen.kimjaeseung.morningbees.createbee.model.RenewalResponse
import com.jasen.kimjaeseung.morningbees.main.MainActivity
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_create_step3.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CreateStep3Activity: AppCompatActivity(), View.OnClickListener, View.OnTouchListener {
    private var clickCnt : Int = 0
    private var jellyCnt : Int = 0

    //intent variables
    lateinit var title : String
    var firstMissionTime : Int = 0
    var lastMissionTime : Int = 0
    private lateinit var accessToken : String
    private lateinit var refreshToken: String

    private val service = MorningBeesService.create()
    private var royalJellyArray : Array<Int> = arrayOf(0,0,0,0,0,0,0,0,0,0,0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_create_step3)
        initButtonListeners()
        initViewListeners()

        onStart()
    }

    override fun onStart() {
        super.onStart()

        if(intent.hasExtra("beename")) {
            intent.getStringExtra("beename")?.let{
                title = intent.getStringExtra("beename")
            }
        }
        else title = ""

        if(intent.hasExtra("firstMissionTime"))
            firstMissionTime = intent.getIntExtra("firstMissionTime", 0)

        if(intent.hasExtra("lastMissionTime"))
            lastMissionTime = intent.getIntExtra("lastMissionTime", 0)

        if(intent.hasExtra("accessToken")){
            accessToken = intent.getStringExtra("accessToken")
        }
        else
            accessToken = ""

        if(intent.hasExtra("refreshToken")){
            refreshToken = intent.getStringExtra("refreshToken")
        }
        else
            refreshToken = ""

        Log.d(TAG, "accessToken: $accessToken")
        Log.d(TAG, "refreshToken: $refreshToken")

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViewListeners(){

        for(i in 1 .. 10){
            val pkg = packageName
            val mId = resources.getIdentifier("jelly_$i", "id", pkg)

            Log.d(TAG, "jelly_$i : $mId")

            val mImageView : ImageView = findViewById(mId)

            royalJellyArray[i] = mId

            Log.d(TAG, "royaljelly[$i].id = $mId .index = $i")

            //mImageView.setOnTouchListener(this)

        }
        create_step3_view.setOnTouchListener(this)
    }

    override fun onClick(v: View) {
        val i = v.id
        when(i){
            R.id.create_step3_next_button -> createBeeServer()
            R.id.go_back_step2_button -> gotoStep2()
            R.id.info_step3_button -> showInfo()
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val imgViewId = v.id
        val action = event.action

        when(action){
           //MotionEvent.ACTION_UP -> changeJellyColor()
            MotionEvent.ACTION_DOWN -> changeJellyColor()
        }

        return false
    }

    private fun initButtonListeners() {
        create_step3_next_button.setOnClickListener(this)
        go_back_step2_button.setOnClickListener(this)
        info_step3_button.setOnClickListener(this)
    }

    private fun changeJellyColor(){
        clickCnt++

        if(clickCnt <= 10){
            jellyCnt = clickCnt

            val mId = royalJellyArray[jellyCnt]
            val mImageView : ImageView = findViewById(mId)
            mImageView.isSelected = true
        }
        else if (clickCnt > 10 && clickCnt <= 20){
            if(clickCnt == 20){
                jellyCnt = 0
                clickCnt = 0

                val mId = royalJellyArray[1]
                val mImageView : ImageView = findViewById(mId)
                mImageView.isSelected = false
            }
            else{
                jellyCnt = 10 - (clickCnt%10)
                val mId = royalJellyArray[jellyCnt+1]
                val mImageView : ImageView = findViewById(mId)
                mImageView.isSelected = false
            }
        }

        selected_jelly_textview.setText((jellyCnt*1000).toString())

        if(jellyCnt < 2){
            create_step3_next_button.isEnabled = false
            create_step3_next_button.background = applicationContext.getDrawable(R.color.deactive_button)
        }
        else{
            create_step3_next_button.isEnabled = true
            create_step3_next_button.background = applicationContext.getDrawable(R.color.active_button)
        }
    }


    private fun createBeeServer(){
        val mPay : Int = (jellyCnt*1000)
        val createBeeRequest = CreateBeeRequest(title, firstMissionTime, lastMissionTime, mPay, " ")
        //val mAccessToken = accessToken
        
        service.createBee(accessToken, createBeeRequest)
            .enqueue(object : Callback<Void> {
                override fun onFailure(call : Call<Void>, t:Throwable){
                    Dlog().d(t.toString())
                }

                override fun onResponse(
                    call : Call<Void>,
                    response: Response<Void>
                ){

                    when (response.code()){
                        201 -> {
                            gotoMain()
                        }

                        400 ->{
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val timestamp = jsonObject.getString("timestamp")
                            val status = jsonObject.getString("status")
                            val message = jsonObject.getString("message")
                            val code = jsonObject.getInt("code")

                            if(code == 101){ // access token 만료 error handling
                                renewalServer()
                            }
                            else { showToast { message }}
                        }

                        500 -> { //internal server error
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val timestamp = jsonObject.getString("timestamp")
                            val status = jsonObject.getString("status")
                            val message = jsonObject.getString("message")
                            val code = jsonObject.getInt("code")

                            showToast { message}
                        }
                    }
                }
            })
    }

    private fun renewalServer(){
        Log.d(TAG, "renewalServer")
        service.renewal(accessToken, refreshToken)
            .enqueue(object : Callback<RenewalResponse>{
                override fun onFailure(call: Call<RenewalResponse>, t: Throwable) {
                    Dlog().d(t.toString())
                }

                override fun onResponse(
                    call: Call<RenewalResponse>,
                    response: Response<RenewalResponse>
                ) {
                    when (response.code()) {
                        200 -> {
                            val renewalResponse = response.body()

                            accessToken = renewalResponse!!.accessToken
                            Log.d(TAG, "renewal accessToken: $accessToken")

                            createBeeServer()
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

                        }
                    }
                }
            })
    }

    private fun  gotoMain(){
        val nextIntent = Intent(this, MainActivity::class.java)
        startActivity(nextIntent)
    }

    private fun gotoStep2(){
        val nextIntent = Intent(this, CreateStep2Activity::class.java)

        nextIntent.putExtra("firstMissionTime", firstMissionTime)
        nextIntent.putExtra("lastMissionTime", lastMissionTime)
        nextIntent.putExtra("accessToken", accessToken)
        nextIntent.putExtra("refreshToken", refreshToken)

        startActivity(nextIntent)
    }

    private fun showInfo(){

    }

    companion object {
        private const val TAG = "CreateStep3Activity"

    }
}


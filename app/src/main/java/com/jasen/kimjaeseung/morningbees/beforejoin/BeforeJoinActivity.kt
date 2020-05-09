package com.jasen.kimjaeseung.morningbees.beforejoin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.createbee.CreateStep1Activity
import com.jasen.kimjaeseung.morningbees.createbee.CreateStep2Activity
import com.jasen.kimjaeseung.morningbees.invitebee.CallbackListener
import com.jasen.kimjaeseung.morningbees.invitebee.InviteBeeActivity
import com.jasen.kimjaeseung.morningbees.invitebee.model.JoinBeeResponse
import com.jasen.kimjaeseung.morningbees.main.MainActivity
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.util.Dlog
import kotlinx.android.synthetic.main.activity_beforejoin.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class BeforeJoinActivity : AppCompatActivity(), View.OnClickListener, CallbackListener
{
    lateinit var refreshToken : String
    lateinit var accessToken: String

    private val service = MorningBeesService.create()

    private val REQUEST_TEST = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beforejoin)

        if(intent.hasExtra("accessToken")){
            accessToken = intent.getStringExtra("accessToken") }

        if(intent.hasExtra("refreshToken")){
            refreshToken = intent.getStringExtra("refreshToken") }

        joinBeeServer()
        initButtonListeners()
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
           R.id.accept_beecreate_button -> gotoBeeCreate()
        }
    }

    private fun initButtonListeners(){
        accept_beecreate_button.setOnClickListener(this)
    }

    private fun showInvitePopup(beeId: String, userId: String){
        val dialogFragment = InviteBeeActivity(this)
        val bundle = Bundle()
        bundle.putString("beeid", beeId)
        dialogFragment.show(supportFragmentManager, "signature")

    }

    private fun joinBeeServer(){
        service.joinBee(accessToken)
            .enqueue(object: Callback<JoinBeeResponse>{
                override fun onFailure(call: Call<JoinBeeResponse>, t: Throwable) {
                    Dlog().d(t.toString())
                }

                override fun onResponse(
                    call: Call<JoinBeeResponse>,
                    response: Response<JoinBeeResponse>
                ) {
                    Dlog().d(response.code().toString())
                    Dlog().d(response.body().toString())
                    Dlog().d(response.headers().toString())
                    Dlog().d(response.errorBody().toString())
                    Dlog().d(response.raw().toString())

                    when(response.code()){
                        200 -> {
                            val joinBeeResponse = response.body()
                            val beeid : Int = joinBeeResponse!!.beeid
                            val userid = joinBeeResponse.userid

                            if(beeid.toString() != "" && userid.toString() != ""){
                                //초대장 받은 경우
                                showInvitePopup(beeid.toString(), userid.toString())
                            }
                        }

                        400 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val timestamp = jsonObject.getString("timestamp")
                            val status = jsonObject.getString("status")
                            val message = jsonObject.getString("message")
                            val code = jsonObject.getInt("code")
                        }

                        500 -> {
                            //interna server error
                        }

                    }
                }
            })
    }

    override fun onDataReceived(data: String) {
        if(data == "accept"){
            //초대받은 bee의 main으로 이동
            val nextIntent : Intent = Intent(this, MainActivity::class.java)
            startActivity(nextIntent)
        }
        else if( data == "deny"){
            // 버튼 눌리면 bee create 화면으로 이동
        }
    }

    private fun gotoBeeCreate(){
        val nextIntent: Intent = Intent(this, CreateStep1Activity::class.java)

        nextIntent.putExtra("accessToken", accessToken)
        nextIntent.putExtra("refreshToken", refreshToken)

        //startActivity(nextIntent)
        startActivityForResult(nextIntent, REQUEST_TEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_TEST){
            if(resultCode == Activity.RESULT_OK){
                if(intent.hasExtra("accessToken")){
                    accessToken = intent.getStringExtra("accessToken")
                }

                if(intent.hasExtra("refreshToken")){
                    refreshToken = intent.getStringExtra("refreshToken")
                }

            }
        }
    }

    companion object {
        private const val TAG = "BeforeJoinActivity"
        private const val RC_SIGN_IN = 9001
        private const val RC_GET_TOKEN = 9002
    }
}
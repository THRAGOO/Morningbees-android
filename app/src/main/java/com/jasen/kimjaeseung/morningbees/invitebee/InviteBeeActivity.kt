package com.jasen.kimjaeseung.morningbees.invitebee

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.beforejoin.BeforeJoinActivity
import com.jasen.kimjaeseung.morningbees.invitebee.model.JoinBeeRequest
import com.jasen.kimjaeseung.morningbees.main.MainActivity
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.util.Dlog

import kotlinx.android.synthetic.main.activity_invite_bee.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InviteBeeActivity : AppCompatActivity(){

    private val service = MorningBeesService.create()
    private lateinit var accessToken : String
    private var userid : Int = 0
    private lateinit var beeid : String
    private lateinit var title : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_bee)

        Log.d(TAG, "InviteBee/onCreate")

        //joinBeeServer()
        beeNameText.text = ("(beeid)에 참여하")

        accept_invitebee_button.setOnClickListener{
            //초대 수락 -> 초대받은 bee main으로 감
            val nextIntent : Intent = Intent(this, MainActivity::class.java)
            startActivity(nextIntent)
            finish()
        }

        close_inviteView_button.setOnClickListener{
            val nextIntent : Intent = Intent(this, BeforeJoinActivity::class.java)
            startActivity(nextIntent)
            finish()
        }
    }

    /*
    private fun joinBeeServer(){
        val joinBeeRequest = JoinBeeRequest(beeid, userid, title)
        service.joinBee(accessToken, joinBeeRequest)
            .enqueue(object: Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Dlog().d(t.toString())
                }

                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {
                    Dlog().d(response.code().toString())
                    Dlog().d(response.body().toString())
                    Dlog().d(response.headers().toString())
                    Dlog().d(response.errorBody().toString())
                    Dlog().d(response.raw().toString())

                    when(response.code()){
                        200 -> {
                            // 수락하면 joinbee 화면 띄울 수 있게 하기
                            // 잘 모르겠다.
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

     */

    companion object{
        val TAG = "InviteBeeActivity"
    }
}



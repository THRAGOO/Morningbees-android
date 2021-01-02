package com.jasen.kimjaeseung.morningbees.invitebee

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.beforejoin.BeforeJoinActivity
import com.jasen.kimjaeseung.morningbees.login.LoginActivity
import com.jasen.kimjaeseung.morningbees.main.MainActivity
import com.jasen.kimjaeseung.morningbees.model.joinbee.JoinBeeRequest
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.showToast

import kotlinx.android.synthetic.main.activity_invite_bee.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InviteBeeActivity : AppCompatActivity(), View.OnClickListener{
    private val service = MorningBeesService.create()
    private var accessToken : String = ""
    private var userid : Int = 0
    private var beeid : Int = 0
    private lateinit var title : String
    private var parameter : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_bee)
        getDynamicLink()
        initButtonListener()
    }

    private fun getDynamicLink(){
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                val deepLink: Uri?
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    parameter = deepLink?.getQueryParameter("beeId").orEmpty()
                    Log.d(TAG, "parameter: $parameter")
                    beeNameText.text = ("${parameter}에 참여하여")
                }
            }
            .addOnFailureListener(this) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }
    }

    private fun initButtonListener(){
        accept_invitebee_button.setOnClickListener(this)
        close_inviteView_button.setOnClickListener(this)
    }

    private fun getAccessToken(){
        if(accessToken == ""){
            startActivity(
                Intent(this, LoginActivity::class.java).putExtra("beeId", beeid)
            )
        } else {
            joinBeeServer()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            100 -> {

            }
        }
    }

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
                            gotoMain()
                        }

                        400 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val message = jsonObject.getString("message")
                            showToast { message }
                        }

                        500 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val message = jsonObject.getString("message")
                            showToast { message }
                        }

                    }
                }
            })
    }

    private fun gotoMain(){
        val nextIntent = Intent(this, MainActivity::class.java)
        startActivity(nextIntent)
        finish()
    }

    private fun gotoBeforeJoin(){
        val nextIntent = Intent(this, BeforeJoinActivity::class.java)
        startActivity(nextIntent)
        finish()
    }

    companion object{
        const val TAG = "InviteBeeActivity"
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.accept_invitebee_button -> getAccessToken()
            R.id.close_inviteView_button -> gotoBeforeJoin()
        }
    }
}



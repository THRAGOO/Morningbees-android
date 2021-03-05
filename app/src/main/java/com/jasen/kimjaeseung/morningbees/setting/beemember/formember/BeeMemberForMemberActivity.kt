package com.jasen.kimjaeseung.morningbees.setting.beemember.formember

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.login.LoginActivity
import com.jasen.kimjaeseung.morningbees.model.beemember.BeeMember
import com.jasen.kimjaeseung.morningbees.model.beemember.BeeMemberResponse
import com.jasen.kimjaeseung.morningbees.model.error.ErrorResponse
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.setting.beemember.formanager.BeeMemberForManagerActivity
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_setting_bee_member_for_member.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response

class BeeMemberForMemberActivity : AppCompatActivity(), View.OnClickListener {
    private var beeMemberList = mutableListOf<BeeMember>()
    private val service = MorningBeesService.create()
    private lateinit var accessToken: String
    private var beeId = 0
    private var managerNickname = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_bee_member_for_member)

        accessToken = GlobalApp.prefs.accessToken
        beeId = GlobalApp.prefsBeeInfo.beeId
        managerNickname = GlobalApp.prefsBeeInfo.beeManagerNickname

        initRecyclerView()
        initButtonListener()
        requestBeeMemberApi()
    }

    private fun requestBeeMemberApi() {
        service.beeMember(accessToken, beeId).enqueue(object : Callback<BeeMemberResponse> {
            override fun onFailure(call: Call<BeeMemberResponse>, t: Throwable) {
                Dlog().d(t.toString())
            }

            override fun onResponse(
                call: Call<BeeMemberResponse>,
                response: Response<BeeMemberResponse>
            ) {
                when (response.code()) {
                    200 -> {
                        val beeMemberResponse = response.body()?.members
                        if (beeMemberResponse == null || beeMemberResponse.size() == 0) {
                            beeMemberList = mutableListOf()
                            initRecyclerView()
                        } else {
                            for (i in 0 until beeMemberResponse.size()) {
                                val item = beeMemberResponse.get(i)
                                val beeMember = BeeMember(
                                    item.asJsonObject.get("nickname").asString,
                                    item.asJsonObject.get("profileImage").asString
                                )
                                beeMemberList.add(beeMember)
                            }
                            sortBeeMemberList()
                            initRecyclerView()
                        }
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
                                gotoLogOut()
                            } else
                                requestBeeMemberApi()
                        } else {
                            showToast { errorResponse.message }
                            finish()
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

    private fun sortBeeMemberList(){
        Log.d(TAG, "manager: ${GlobalApp.prefsBeeInfo.beeManagerNickname}")
        for(i in 0 until beeMemberList.size-1){
            if(beeMemberList[i].nickname == GlobalApp.prefsBeeInfo.beeManagerNickname){

                beeMemberList.addAll(0, listOf(beeMemberList[i]))
                beeMemberList.removeAt(i+1)
            }
        }
    }


    private fun initRecyclerView() {
        beeMemberRecyclerForMember.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter =
                BeeMemberForMemberAdapter(
                    beeMemberList,
                    this@BeeMemberForMemberActivity
                )
        }
    }

    private fun initButtonListener() {
        toSettingBeeMemberActivityButtonForMember.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.toSettingBeeMemberActivityButtonForMember -> finish()
        }
    }

    private fun gotoLogOut() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .putExtra("RequestLogOut", "")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)        )
    }

    companion object {
        const val TAG = "BeeMemberForMemeber"
    }
}
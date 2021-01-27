package com.jasen.kimjaeseung.morningbees.setting.beemember.formanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.iosParameters
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.model.beemember.BeeMember
import com.jasen.kimjaeseung.morningbees.model.beemember.BeeMemberResponse
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_setting_bee_member_for_manager.*
import kotlinx.android.synthetic.main.item_bee_member_for_manager.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BeeMemberForManagerActivity : AppCompatActivity(), View.OnClickListener {
    private var beeMemberList = mutableListOf<BeeMember>()
    private val service = MorningBeesService.create()
    private lateinit var accessToken: String
    private var beeId = 0
    var managerNickname = ""
    private var beeTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_bee_member_for_manager)

        accessToken = GlobalApp.prefs.accessToken
        beeId = GlobalApp.prefsBeeInfo.beeId
        managerNickname = GlobalApp.prefsBeeInfo.beeManagerNickname
        beeTitle = GlobalApp.prefsBeeInfo.beeTitle

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
                        val jsonObject = JSONObject(response.errorBody()?.string())
                        val message = jsonObject.getString("message")
                        val code = jsonObject.getInt("code")
                        showToast { message }
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

        val beeMemberSwipeHelperCallback = BeeMemberSwipeHelperCallback()
            .apply {
                setClamp(300f)
            }
        val itemTouchHelper = ItemTouchHelper(beeMemberSwipeHelperCallback)
        itemTouchHelper.attachToRecyclerView(beeMemberRecyclerForManager)

        beeMemberRecyclerForManager.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter =
                BeeMemberForManagerAdapter(
                    beeMemberList,
                    this@BeeMemberForManagerActivity
                )
            setOnTouchListener { _,  _ ->
                beeMemberSwipeHelperCallback.removePreviousClamp(this)
                false
            }
        }
    }

    private fun initButtonListener() {
        toSettingBeeMemberActivityButtonForManager.setOnClickListener(this)
        copyInviteLinkButtonForManager.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.toSettingBeeMemberActivityButtonForManager -> finish()
            R.id.copyInviteLinkButtonForManager -> createDynamicLink()
        }
    }

    // MARK:~ Dynamic Link

    private fun createDynamicLink() {
        val shortLinkTask = Firebase.dynamicLinks.shortLinkAsync {
            //link = Uri.parse("https://www.app.thragoo.com?beeId=$beeId?beeTitle=$beeTitle")
            link = Uri.parse("https://www.app.thragoo.com").buildUpon()
                .appendQueryParameter("beeId", beeId.toString())
                .appendQueryParameter("beeTitle", beeTitle)
                .build()
            domainUriPrefix = "https://thragoo.page.link"
            iosParameters("com.thragoo.Morningbees-iOS") {}
        }.addOnCompleteListener(this, OnCompleteListener<ShortDynamicLink>() {
            if (it.isSuccessful) {
                val shortLink = it.result?.shortLink
                val strLink = shortLink.toString()
                shareLink(strLink)
            }
        })
    }

    private fun shareLink(shortLink: String) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, "Try this amazing app: $shortLink")
        startActivity(Intent.createChooser(intent, "Share Link"))
    }

    companion object {
        const val TAG = "BeeMemberForManager"
    }
}
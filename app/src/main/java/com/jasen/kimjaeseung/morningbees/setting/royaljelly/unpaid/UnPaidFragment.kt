package com.jasen.kimjaeseung.morningbees.setting.royaljelly.unpaid

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.model.beepenalty.BeePenaltyResponse
import com.jasen.kimjaeseung.morningbees.model.error.ErrorResponse
import com.jasen.kimjaeseung.morningbees.model.paid.Paid
import com.jasen.kimjaeseung.morningbees.model.penalty.Penalty
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.setting.royaljelly.RoyalJellyActivity
import com.jasen.kimjaeseung.morningbees.util.Dlog
import kotlinx.android.synthetic.main.activity_royaljelly.*
import kotlinx.android.synthetic.main.fragment_part_payment.*
import kotlinx.android.synthetic.main.fragment_royaljelly_unpaid.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import java.text.DecimalFormat

class UnPaidFragment : Fragment(), UnPaidAdapter.OnItemSelectedInterface {
    private val service = MorningBeesService.create()
    var penaltiesList = mutableListOf<Penalty>()
    lateinit var selectedList: Array<Boolean>
    var userId: Long = 0

    lateinit var partPaymentList : Paid

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_royaljelly_unpaid, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        singleChoiceLayout.visibility = View.INVISIBLE
        multipleChoiceLayout.visibility = View.INVISIBLE

        initButtonListener()
        requestBeePenaltyApi(UNPAID_STATUS)
    }

    private fun initButtonListener() {
        fullPaymentInMultipleChoiceButton.setOnClickListener(activity as RoyalJellyActivity)
        fullPaymentInSingleChoiceButton.setOnClickListener(activity as RoyalJellyActivity)
        searchBeeMemberButton.setOnClickListener(activity as RoyalJellyActivity)

        partPayoutInSingleChoiceButton.setOnClickListener {
            (activity as RoyalJellyActivity).setPartPaymentDialog(partPaymentList)
        }
    }

    override fun onItemSelected(v: View, position: Int) {
        v.isSelected = !v.isSelected
        selectedList[position] = v.isSelected
        partPaymentList = Paid(penaltiesList[position].penalty, penaltiesList[position].userId)
        changeButton()
    }

    private fun changeButton() {
        var count = 0
        for (i in 0 until selectedList.size - 1) {
            if (selectedList[i])
                count++
        }

        when {
            count == 1 -> {
                singleChoiceLayout.visibility = View.VISIBLE
                multipleChoiceLayout.visibility = View.INVISIBLE
            }
            count > 1 -> {
                singleChoiceLayout.visibility = View.INVISIBLE
                multipleChoiceLayout.visibility = View.VISIBLE
                fullPaymentInMultipleChoiceButton.text = "선택한 ${count}명, 전액 납부"
            }
            else -> {
                singleChoiceLayout.visibility = View.INVISIBLE
                multipleChoiceLayout.visibility = View.INVISIBLE
            }
        }
    }

    fun setRecyclerView() {
        unPaidRoyalJellyRecyclerView.adapter = UnPaidAdapter(penaltiesList, this)
        unPaidRoyalJellyRecyclerView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
    }

    private fun Int.getPriceAnnotation(): String {
        return DecimalFormat("###,###").format(this)
    }

    private fun requestBeePenaltyApi(status: Int) {
        service.beePenalty(GlobalApp.prefs.accessToken, GlobalApp.prefsBeeInfo.beeId, status)
            .enqueue(object : Callback<BeePenaltyResponse> {
                override fun onFailure(call: Call<BeePenaltyResponse>, t: Throwable) {
                    Dlog().d(t.toString())
                }

                override fun onResponse(
                    call: Call<BeePenaltyResponse>,
                    response: Response<BeePenaltyResponse>
                ) {
                    when (response.code()) {
                        200 -> {
                            val penaltyHistoriesResponse = response.body()?.penaltyHistories
                            val penaltiesResponse = response.body()?.penalties

                            penaltiesList = mutableListOf()

                            if (penaltyHistoriesResponse?.size() == 0 || penaltyHistoriesResponse == null) {
                                totalRoyalJelly.text = "0"
                            } else {
                                if (penaltyHistoriesResponse.size() > 0) {
                                    lateinit var unPaidPenaltyHistory: JsonObject
                                    lateinit var paidPenaltyHistory: JsonObject

                                    if (penaltyHistoriesResponse[0].asJsonObject != null) {
                                        unPaidPenaltyHistory =
                                            penaltyHistoriesResponse[0].asJsonObject
                                        GlobalApp.prefsBeeInfo.unPaidPenalty =
                                            unPaidPenaltyHistory.get("total").asInt
                                        totalUnPaidRoyalJelly.text = GlobalApp.prefsBeeInfo.unPaidPenalty.getPriceAnnotation()

                                    }
                                    if (penaltyHistoriesResponse[1].asJsonObject != null) {
                                        paidPenaltyHistory =
                                            penaltyHistoriesResponse[1].asJsonObject
                                        GlobalApp.prefsBeeInfo.paidPenalty =
                                            paidPenaltyHistory.get("total").asInt
                                        (activity as RoyalJellyActivity).initLayout()
                                    }
                                }
                            }

                            if (penaltiesResponse != null) {
                                if (penaltiesResponse.size() > 0) {
                                    for (i in 0 until penaltiesResponse.size()) {
                                        val penalties = penaltiesResponse[i].asJsonObject
                                        val nickname = penalties.get("nickname").asString
                                        val penalty = penalties.get("penalty").asInt
                                        val userId = penalties.get("userId").asLong

                                        if (penalty != 0) {
                                            Log.d(TAG, "nickname: $nickname penalty: $penalty")
                                            penaltiesList.add(Penalty(nickname, penalty, userId))
                                        }
                                    }
                                }
                            }

                            Log.d(TAG, "penaltiesList: $penaltiesList")
                            setRecyclerView()
                            selectedList = Array(penaltiesList.size + 1) { i -> false }
                            (activity as RoyalJellyActivity).initPrintedList = penaltiesList
                        }

                        400 -> {
                            val converter: Converter<ResponseBody, ErrorResponse> =
                                MorningBeesService.retrofit.responseBodyConverter<ErrorResponse>(
                                    ErrorResponse::class.java,
                                    ErrorResponse::class.java.annotations
                                )

                            val errorResponse = converter.convert(response.errorBody())

                            if (errorResponse.code == 111 || errorResponse.code == 110 || errorResponse.code == 120) {
                                val oldAccessToken = GlobalApp.prefs.accessToken
                                GlobalApp.prefs.requestRenewalApi()
                                val renewalAccessToken = GlobalApp.prefs.accessToken

                                if (oldAccessToken == renewalAccessToken) {
//                                    showToast { "다시 로그인해주세요." }
                                    (activity as RoyalJellyActivity).gotoLogOut()
                                } else
                                    requestBeePenaltyApi(status)
                            } else {
                                (activity as RoyalJellyActivity).finish()
                            }
                        }

                        500 -> {
                            val jsonObject = JSONObject(response.errorBody()?.string())
                            val message = jsonObject.getString("message")
//                            showToast { message }
                        }
                    }
                }
            })
    }

    companion object {
        const val TAG = "RoyalJellyActivity"
        const val UNPAID_STATUS = 0
    }

}
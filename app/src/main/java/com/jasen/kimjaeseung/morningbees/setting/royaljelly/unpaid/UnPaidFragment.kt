package com.jasen.kimjaeseung.morningbees.setting.royaljelly.unpaid

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.model.beepenalty.BeePenaltyResponse
import com.jasen.kimjaeseung.morningbees.model.penalty.Penalty
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.util.Dlog
import kotlinx.android.synthetic.main.fragment_royaljelly_unpaid.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class UnPaidFragment : Fragment() {
    private val service = MorningBeesService.create()
    private var penaltiesList = mutableListOf<Penalty>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_royaljelly_unpaid, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestBeePenaltyApi(UNPAID_STATUS)
    }

    private fun setRecyclerView() {
        unPaidRoyalJellyRecyclerView.adapter = UnPaidAdapter(penaltiesList)
        unPaidRoyalJellyRecyclerView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, true)

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
                    val penaltyHistoriesResponse = response.body()?.penaltyHistories
                    val penaltiesResponse = response.body()?.penalties

                    if (penaltyHistoriesResponse?.size() == 0 || penaltyHistoriesResponse == null) {
                        totalUnPaidRoyalJelly.text = "0"
                    } else {
                        if (penaltyHistoriesResponse.size() > 0) {
                            val unpaidPenaltyHistories = penaltyHistoriesResponse[1].asJsonObject

                            totalUnPaidRoyalJelly.text =
                                unpaidPenaltyHistories.get("total").asInt.getPriceAnnotation()
                        }
                    }

                    Log.d(TAG, " penaltiesResponse.size: ${penaltiesResponse?.size()}")
                    if (penaltiesResponse != null) {
                        if (penaltiesResponse.size() > 0) {
                            for (i in 0 until penaltiesResponse.size()) {
                                val penalties = penaltiesResponse[i].asJsonObject
                                val nickname = penalties.get("nickname").asString
                                val penalty = penalties.get("penalty").asInt

                                penaltiesList.add(Penalty(nickname, penalty))
                            }
                        }
                    }
                    setRecyclerView()
                }
            })
    }

    companion object {
        const val TAG = "RoyalJellyActivity"
        const val UNPAID_STATUS = 0
    }

}
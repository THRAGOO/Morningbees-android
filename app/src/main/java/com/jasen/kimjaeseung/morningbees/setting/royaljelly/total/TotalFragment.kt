package com.jasen.kimjaeseung.morningbees.setting.royaljelly.total

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
import com.jasen.kimjaeseung.morningbees.setting.royaljelly.unpaid.UnPaidFragment
import com.jasen.kimjaeseung.morningbees.util.Dlog
import kotlinx.android.synthetic.main.fragment_royaljelly_total.*
import kotlinx.android.synthetic.main.fragment_royaljelly_unpaid.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class TotalFragment : Fragment() {
    private val service = MorningBeesService.create()
    private val totalList = mutableListOf<Penalty>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_royaljelly_total, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestBeePenaltyApi(PAID_STATUS)
    }

    private fun setRecyclerView(){
        totalRoyalJellyRecyclerView.adapter = TotalAdapter(totalList)
        totalRoyalJellyRecyclerView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, true)
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
                    val penaltiesResponse = response.body()?.penalties

                    Log.d(UnPaidFragment.TAG, " penaltiesResponse.size: ${penaltiesResponse?.size()}")
                    if (penaltiesResponse != null) {
                        if (penaltiesResponse.size() > 0) {
                            for (i in 0 until penaltiesResponse.size()) {
                                val penalties = penaltiesResponse[i].asJsonObject
                                val nickname = penalties.get("nickname").asString
                                val penalty = penalties.get("penalty").asInt

                                totalList.add(Penalty(nickname, penalty))
                            }
                        }
                    }
                    setRecyclerView()
                }
            })
    }

    companion object {
        const val TAG = "RoyalJellyActivity"
        const val PAID_STATUS = 1
    }
}
package com.jasen.kimjaeseung.morningbees.setting.royaljelly

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.model.beepenalty.BeePenaltyResponse
import com.jasen.kimjaeseung.morningbees.model.penalty.Penalty
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.setting.royaljelly.total.TotalFragment
import com.jasen.kimjaeseung.morningbees.setting.royaljelly.unpaid.UnPaidFragment
import com.jasen.kimjaeseung.morningbees.util.Dlog
import kotlinx.android.synthetic.main.activity_royaljelly.*
import kotlinx.android.synthetic.main.fragment_royaljelly_unpaid.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class RoyalJellyActivity : View.OnClickListener, FragmentActivity() { // AppCompatActivity() 생략
//    private var pay = 0
    private val service = MorningBeesService.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_royaljelly)
        requestBeePenaltyApi(UNPAID_STATUS)
        setFragment(UNPAID_STATUS)
        initButtonListener()
    }

    private fun initButtonListener() {
        unPaidRoyallJellyButton.setOnClickListener(this)
        totalRoyalJellyButton.setOnClickListener(this)
        goToSettingFromRoyalJelly.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.unPaidRoyallJellyButton -> setFragment(UNPAID_STATUS)
            R.id.totalRoyalJellyButton -> setFragment(PAID_STATUS)
            R.id.goToSettingFromRoyalJelly -> finish()
        }
    }

    private fun Int.getPriceAnnotation(): String {
        return DecimalFormat("###,###").format(this)
    }

    private fun setFragment(status: Int) {
        when (status) {
            UNPAID_STATUS -> {
                unPaidRoyallJellyButton.isSelected = true
                totalRoyalJellyButton.isSelected = false

                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer,
                        UnPaidFragment()
                    )
                    .commit()
            }

            PAID_STATUS -> {
                unPaidRoyallJellyButton.isSelected = false
                totalRoyalJellyButton.isSelected = true

                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer,
                        TotalFragment()
                    )
                    .commit()
            }
        }
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
                        totalRoyalJelly.text = "0"
                    } else {
                        if (penaltyHistoriesResponse.size() > 0) {
                            val totalPenaltyHistories = penaltyHistoriesResponse[0].asJsonObject
                            totalRoyalJelly.text = totalPenaltyHistories.get("total").asInt.getPriceAnnotation()
                        }
                    }
                }
            })
    }

    companion object {
        const val TAG = "RoyalJellyActivity"
        const val UNPAID_STATUS = 0
        const val PAID_STATUS = 1
    }
}
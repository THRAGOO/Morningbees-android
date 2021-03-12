package com.jasen.kimjaeseung.morningbees.setting.royaljelly

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.login.LoginActivity
import com.jasen.kimjaeseung.morningbees.model.error.ErrorResponse
import com.jasen.kimjaeseung.morningbees.model.paid.Paid
import com.jasen.kimjaeseung.morningbees.model.paid.PaidRequest
import com.jasen.kimjaeseung.morningbees.model.penalty.Penalty
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.setting.royaljelly.search.SearchPenaltyFragment
import com.jasen.kimjaeseung.morningbees.setting.royaljelly.total.TotalFragment
import com.jasen.kimjaeseung.morningbees.setting.royaljelly.unpaid.UnPaidFragment
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_royaljelly.*
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import java.text.DecimalFormat

class RoyalJellyActivity : View.OnClickListener, FragmentActivity(),
    DialogInterface.OnDismissListener {
    private val service = MorningBeesService.create()

    var printedList = mutableListOf<Penalty>()
    var initPrintedList = mutableListOf<Penalty>()
    var searchState = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_royaljelly)
        setFragment(UNPAID_STATUS)
        initButtonListener()
    }

    fun initLayout() {
        totalRoyalJelly.text = GlobalApp.prefsBeeInfo.paidPenalty.getPriceAnnotation()
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (searchState == TOTAL_STATUS) {
            val totalFragment =
                supportFragmentManager.findFragmentById(R.id.fragmentContainer) as TotalFragment
            totalFragment.totalList = printedList
            totalFragment.setRecyclerView()
        } else {
            val unPaidFragment =
                supportFragmentManager.findFragmentById(R.id.fragmentContainer) as UnPaidFragment
            unPaidFragment.penaltiesList = printedList
        }
    }

    private fun Int.getPriceAnnotation(): String {
        return DecimalFormat("###,###").format(this)
    }

    private fun initButtonListener() {
        unPaidRoyallJellyButton.setOnClickListener(this)
        totalRoyalJellyButton.setOnClickListener(this)
        goToSettingFromRoyalJelly.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.unPaidRoyallJellyButton -> setFragment(UNPAID_STATUS)
            R.id.totalRoyalJellyButton -> setFragment(TOTAL_STATUS)
            R.id.goToSettingFromRoyalJelly -> finish()

            // button in UnPaidFragment
            R.id.fullPaymentInMultipleChoiceButton -> setPaidApi(FULL_PAYMENT_STATE)
            R.id.fullPaymentInSingleChoiceButton -> setPaidApi(FULL_PAYMENT_STATE)
//            R.id.partPayoutInSingleChoiceButton -> setPartPaymentDialog()
            R.id.searchBeeMemberButton -> setSearchDialog()

            // button in TotalFragment
            R.id.searchBeeMemberInTotalButton -> setSearchDialog()

            // button in PartPaymentFragment
            R.id.selectedPaymentButton -> setPaidApi(PART_PAYMENT_STATE)
        }
    }

    private fun setSearchDialog() {
        val searchPenaltyFragment = SearchPenaltyFragment()

        if (searchState == TOTAL_STATUS) {
            val totalFragment =
                supportFragmentManager.findFragmentById(R.id.fragmentContainer) as TotalFragment
            printedList = initPrintedList
        } else {
            val unPaidFragment =
                supportFragmentManager.findFragmentById(R.id.fragmentContainer) as UnPaidFragment
            printedList = initPrintedList
        }

        searchPenaltyFragment.run {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
            show(supportFragmentManager, TAG)
        }
    }

    private fun setFragment(status: Int) {
        searchState = status
        totalRoyalJelly.text = "0"
        when (status) {
            UNPAID_STATUS -> {
                unPaidRoyallJellyButton.isSelected = true
                totalRoyalJellyButton.isSelected = false

                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        UnPaidFragment()
                    )
                    .commit()
            }

            TOTAL_STATUS -> {
                unPaidRoyallJellyButton.isSelected = false
                totalRoyalJellyButton.isSelected = true

                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        TotalFragment()
                    )
                    .commit()
            }
        }
    }

    fun setPaidApi(state: Int) {
        if (state == PART_PAYMENT_STATE) {
            val penalties = sendArray(PART_PAYMENT_STATE)
            requestPaidApi(penalties)
        } else {
            val penalties = sendArray(FULL_PAYMENT_STATE)
            requestPaidApi(penalties)
        }
    }

    private fun requestPaidApi(penalties: List<Paid>) {
        val paidRequest = PaidRequest(penalties)

        service.paid(
            GlobalApp.prefs.accessToken,
            GlobalApp.prefsBeeInfo.beeId,
            paidRequest
        )
            .enqueue(object : Callback<Void> {
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Dlog().d(t.toString())
                }

                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    when (response.code()) {
                        200 -> {
                            setFragment(UNPAID_STATUS)
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
                                    showToast { "다시 로그인해주세요." }
                                    gotoLogOut()
                                } else
                                    requestPaidApi(penalties)
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

    private fun sendArray(state: Int): List<Paid> {
        var list = listOf<Paid>()
        val mutableList = mutableListOf<Paid>()

        if (state == FULL_PAYMENT_STATE) {
            val unPaidFragment =
                supportFragmentManager.findFragmentById(R.id.fragmentContainer) as UnPaidFragment
            val penaltiesList = unPaidFragment.penaltiesList
            val selectedList = unPaidFragment.selectedList

            try {
                for (i in 0 until penaltiesList.size) {
                    if (selectedList[i]) {
                        mutableList.add(Paid(penaltiesList[i].penalty, penaltiesList[i].userId))
                    }
                }
                list = mutableList
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            try {
                mutableList.add(Paid(GlobalApp.prefsBeeInfo.selectedPartPayment, GlobalApp.prefsBeeInfo.selectedUserId))
                list = mutableList
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return list
    }

    fun setPartPaymentDialog(list: Paid) {
        PartPaymentFragment(list).run {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
            show(supportFragmentManager, TAG)
        }
    }

    fun gotoLogOut() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .putExtra("RequestLogOut", "")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }


    companion object {
        const val TAG = "RoyalJellyActivity"
        const val UNPAID_STATUS = 0
        const val TOTAL_STATUS = 1
        const val FULL_PAYMENT_STATE = 2
        const val PART_PAYMENT_STATE = 3
    }
}
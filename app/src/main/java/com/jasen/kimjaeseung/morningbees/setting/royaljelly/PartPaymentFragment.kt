package com.jasen.kimjaeseung.morningbees.setting.royaljelly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.model.paid.Paid
import com.jasen.kimjaeseung.morningbees.util.getPriceAnnotation
import kotlinx.android.synthetic.main.fragment_part_payment.*

class PartPaymentFragment(val partPaymentList: Paid) :
    BottomSheetDialogFragment() {

    // Properties

    var penalty = 0

    // Life Cycle

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_part_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedPayment.text = "0"
        thumbSeekBar.isEnabled = false
        initButtonListener()
        initSeekBar()

        maxPaymentSeekBar.text = partPaymentList.penalty.getPriceAnnotation()
        selectSeekBar.max = (partPaymentList.penalty) / 1000
        thumbSeekBar.max = selectSeekBar.max
    }

    // Init Method

    private fun initButtonListener() {
        selectedPaymentButton.setOnClickListener {
            (activity as RoyalJellyActivity).setPaidApi(PART_PAYMENT_STATE)
            dismiss()
        }
    }

    private fun initSeekBar() {
        selectSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress * 1000 <= partPaymentList.penalty) {
                    penalty = progress * 1000
                    GlobalApp.prefsBeeInfo.selectedPartPayment = penalty
                    GlobalApp.prefsBeeInfo.selectedUserId = partPaymentList.userId
                    selectedPayment.text = penalty.getPriceAnnotation()

                    thumbSeekBar.setProgress(progress, true)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    companion object {
        const val PART_PAYMENT_STATE = 3
        const val TAG = "PartPaymentFragment"
    }
}
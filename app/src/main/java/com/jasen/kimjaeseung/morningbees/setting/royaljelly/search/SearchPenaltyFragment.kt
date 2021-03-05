package com.jasen.kimjaeseung.morningbees.setting.royaljelly.search

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.model.paid.Paid
import com.jasen.kimjaeseung.morningbees.model.penalty.Penalty
import com.jasen.kimjaeseung.morningbees.setting.royaljelly.RoyalJellyActivity
import kotlinx.android.synthetic.main.fragment_search_penalty_list.*

class SearchPenaltyFragment : BottomSheetDialogFragment(), SearchPenaltyAdapter.OnItemSelectedInterface {
    var beeList = mutableListOf<Penalty>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_search_penalty_list, container)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return rootView
//        return inflater.inflate(R.layout.fragment_search_penalty_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showKeyboard()
        initRecyclerView()
        initEditText()
    }

    private fun initEditText(){
        searchTextInputLayer.setEndIconDrawable(R.drawable.icon_delete_all)
        searchKeywordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                matchKeyword(p0)
            }
        })
    }

    private fun matchKeyword(keyWord : CharSequence?){
        beeList = mutableListOf()
        val printedList = (activity as RoyalJellyActivity).printedList
        if(keyWord.toString().count() != 0 && keyWord != null){
            for(i in 0 until printedList.size){
                if (printedList[i].nickname.contains(keyWord, true)){
                    val item = Penalty(printedList[i].nickname, printedList[i].penalty, printedList[i].userId)
                    beeList.add(item)
                }
            }
        }
        initRecyclerView()
    }

    private fun initRecyclerView() {
        searchPenaltyRecyclerView.adapter = SearchPenaltyAdapter(beeList, this)
        searchPenaltyRecyclerView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
    }

    override fun onItemSelected(v: View, position: Int) {
        val printedList = mutableListOf<Penalty>()
        printedList.add(Penalty(beeList[position].nickname, beeList[position].penalty, beeList[position].userId))
        (activity as RoyalJellyActivity).setPartPaymentDialog(Paid(beeList[position].penalty, beeList[position].userId))
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Log.d(TAG, "onDismiss")
        hideKeyboard()
        val activity = activity
        if (activity is DialogInterface.OnDismissListener) {
            (activity as DialogInterface.OnDismissListener).onDismiss(dialog)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        if(dialog is BottomSheetDialog){
            dialog.behavior.skipCollapsed = true
            dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    private fun showKeyboard(){
        if(searchKeywordEditText.requestFocus()){
            (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
        }
    }

    private fun hideKeyboard(){
        (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        hideKeyboard()
    }

    companion object {
        const val TAG = "SearchPenaltyFragment"
    }
}
package com.jasen.kimjaeseung.morningbees.setting.beemember.formanager

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.model.beemember.BeeMember
import kotlinx.android.synthetic.main.item_bee_member_for_manager.view.*

class BeeMemberForManagerAdapter(
    private val beeMemberList: MutableList<BeeMember>,
    private val context: Context
) : RecyclerView.Adapter<BeeMemberForManagerAdapter.BeeMemberViewHolderForManager>() {
    private val managerNickname = GlobalApp.prefsBeeInfo.beeManagerNickname
    private val items = mutableListOf<BeeMember>().apply {
        for (i in 0 until beeMemberList.size)
            add(beeMemberList[i])
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BeeMemberViewHolderForManager {
        return BeeMemberViewHolderForManager(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_bee_member_for_manager, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: BeeMemberViewHolderForManager, position: Int) {
        holder.bind(items[position], managerNickname)
    }

    class BeeMemberViewHolderForManager(view: View) : RecyclerView.ViewHolder(view) {
        private val itemProfile = itemView.itemBeeMemberProfileForManager
        private val itemNickname = itemView.itemBeeMemberNicknameForManager

        fun bind(beeMember: BeeMember, managerNickname: String) {
            Glide.with(itemView.itemBeeMemberProfileForManager)
                .load(beeMember.profileImage)
                .centerCrop()
                .into(itemProfile)

            itemNickname.text = beeMember.nickname
            if (managerNickname == beeMember.nickname)
                itemView.itemBeeMemberManagerForManager.setImageResource(R.drawable.icon_crown)
        }
    }
}
package com.jasen.kimjaeseung.morningbees.setting.beemember.formember

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
import kotlinx.android.synthetic.main.item_bee_member_for_member.view.*

class BeeMemberForMemberAdapter(
    private val beeMemberList: MutableList<BeeMember>,
    private val context: Context
) : RecyclerView.Adapter<BeeMemberForMemberAdapter.BeeMemberViewHolderForMember>() {
    private val managerNickname = GlobalApp.prefsBeeInfo.beeManagerNickname

    private val items = mutableListOf<BeeMember>().apply {
        for (i in 0 until beeMemberList.size)
            add(beeMemberList[i])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeeMemberViewHolderForMember {
        return BeeMemberViewHolderForMember(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_bee_member_for_member, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return beeMemberList.size
    }

    override fun onBindViewHolder(holder: BeeMemberViewHolderForMember, position: Int) {
        holder.bind(items[position], managerNickname)
    }

    class BeeMemberViewHolderForMember(view: View) : RecyclerView.ViewHolder(view) {
        private val itemProfile = itemView.itemBeeMemberProfileForMember
        private val itemNickname = itemView.itemBeeMemberNicknameForMember

        fun bind(beeMember: BeeMember, managerNickname: String) {
            Glide.with(itemView.itemBeeMemberProfileForMember)
                .load(R.drawable.default_profile_image)
                .centerCrop()
                .into(itemProfile)

            itemNickname.text = beeMember.nickname
            if (managerNickname == beeMember.nickname)
                itemView.itemBeeMemberManagerForMember.setImageResource(R.drawable.icon_crown)
        }
    }
}
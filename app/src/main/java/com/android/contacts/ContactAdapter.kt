package com.android.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.contacts.databinding.SingleContactViewBinding


class ContactAdapter(
    private val contactVOList: List<ContactInfo>) :
    RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding =
            SingleContactViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ContactViewHolder,
        position: Int
    ) {
        val contactVO: ContactInfo = contactVOList[position]
        holder.tvContactName.text = contactVO.name
        holder.tvPhoneNumber.text = contactVO.number
        contactVO.image?.let {
            holder.ivContactImage.setImageBitmap(it)
        }?: run {
            holder.ivContactImage.setImageResource(R.drawable.ic_launcher_background)
        }
    }

    override fun getItemCount(): Int {
        return contactVOList.size
    }

    class ContactViewHolder(binding: SingleContactViewBinding) : RecyclerView.ViewHolder(binding.root) {
        var ivContactImage = binding.ivContactImage
        var tvContactName = binding.tvContactName
        var tvPhoneNumber = binding.tvPhoneNumber

    }

}
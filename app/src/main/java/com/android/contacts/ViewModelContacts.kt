package com.android.contacts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelContacts : ViewModel() {
    val livDataContacts: MutableLiveData< MutableList<ContactInfo>> by lazy {
        MutableLiveData< MutableList<ContactInfo>>()
    }


    fun getTransferContact(number:String) =livDataContacts.value?.single { it.number.contains(number) }




}
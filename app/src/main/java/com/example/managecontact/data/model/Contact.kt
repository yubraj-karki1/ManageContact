package com.example.managecontact.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contact(
    var id: String = "",
    var name: String = "",
    var phoneNumber: String = "",
    var userId: String = "",
    var createdAt: Long = System.currentTimeMillis()
) : Parcelable 
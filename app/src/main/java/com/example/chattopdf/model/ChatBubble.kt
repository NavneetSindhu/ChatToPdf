package com.example.chattopdf.model

import android.net.Uri
import java.io.File

data class ChatBubble(
    val text:String,
    val isBot:Boolean,
    val attachment: File?=null,
    val userImages:List<Uri> = emptyList<Uri>()
)

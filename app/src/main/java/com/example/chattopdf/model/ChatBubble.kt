package com.example.chattopdf.model

import java.io.File

data class ChatBubble(
    val text:String,
    val isBot:Boolean,
    val attachment: File?=null
)

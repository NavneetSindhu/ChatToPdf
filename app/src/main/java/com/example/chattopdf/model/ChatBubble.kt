package com.example.chattopdf.model

import android.net.Uri
import java.io.File

data class ChatBubble(
    val messageId: Long = 0,      // Link back to Room
    val sessionId: Long = 0,      // Link back to the project
    val text: String,
    val isBot: Boolean,
    val attachment: File? = null,
    val userImages: List<Uri> = emptyList(),
    val suggestions: List<String> = emptyList()
)

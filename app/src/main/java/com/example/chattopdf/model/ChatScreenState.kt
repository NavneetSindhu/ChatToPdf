package com.example.chattopdf.model

sealed class ChatScreenState(val options:List<String>) {
    object Welcome: ChatScreenState(
        options = listOf("For Printing", "For Sharing", "Wait, crop it first")
    )
    object AskDocType: ChatScreenState(
        options = listOf("Notes/Book", "ID Card", "Receipt", "Other")
    )
    object AskPageOrientation: ChatScreenState(
        options = listOf("A4", "Original Size")
    )

    object Processing : ChatScreenState(
        options = emptyList()
    )
}
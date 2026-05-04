package com.example.chattopdf.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSession::class,
            parentColumns = ["sessionId"],
            childColumns = ["parentSessionId"],
            onDelete = ForeignKey.CASCADE // If session is deleted, delete all messages
        )
    ]
)
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val messageId: Long = 0,
    val parentSessionId: Long,
    val text: String,
    val isBot: Boolean,
    val imagePaths: List<String> = emptyList(),
    val hasPdfAttachment: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
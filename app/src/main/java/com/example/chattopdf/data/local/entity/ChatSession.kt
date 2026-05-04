package com.example.chattopdf.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey(autoGenerate = true) val id:Long=0,
    val title:String,
    val generatedPdfPath:String?=null,
    val createdAt:Long = System.currentTimeMillis()
)

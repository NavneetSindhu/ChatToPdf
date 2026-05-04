package com.example.chattopdf.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.chattopdf.data.local.entity.ChatMessage
import com.example.chattopdf.data.local.entity.ChatSession
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    // --- SESSION QUERIES ---
    @Insert
    suspend fun insertSession(session: ChatSession): Long

    @Query("SELECT * FROM chat_sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<ChatSession>>

    @Update
    suspend fun updateSession(session: ChatSession)

    // --- MESSAGE QUERIES ---
    @Insert
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("SELECT * FROM chat_messages WHERE parentSessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessage>>

    @Query("DELETE FROM chat_messages WHERE parentSessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: Long)
}
package com.example.chattopdf.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.chattopdf.data.local.converter.Converters
import com.example.chattopdf.data.local.dao.ChatDao
import com.example.chattopdf.data.local.entity.ChatMessage
import com.example.chattopdf.data.local.entity.ChatSession


@Database(
    entities = [ChatSession::class, ChatMessage::class],
    version = 1,
    exportSchema = false

)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase?=null

        fun getDatabase(context: Context): AppDatabase{
            return INSTANCE?:synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pdf_app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
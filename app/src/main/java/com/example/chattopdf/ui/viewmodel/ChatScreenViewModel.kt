package com.example.chattopdf.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import com.example.chattopdf.model.ChatBubble
import com.example.chattopdf.model.ChatScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatScreenViewModel: ViewModel() {

    private val _currentState = MutableStateFlow<ChatScreenState>(ChatScreenState.Welcome)
    val currentState = _currentState.asStateFlow()

    val listDummy = listOf(ChatBubble("njnj",true),ChatBubble("njnj",false),ChatBubble("njnj",true),ChatBubble("njnj",false))

    private val _currentChats = MutableStateFlow<List<ChatBubble>>(listDummy)
    val currentChats = _currentChats.asStateFlow()

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages = _selectedImages.asStateFlow()


    fun onImageSelected(uris:List<Uri>){
        if(uris.isNotEmpty()){
            _selectedImages.value += uris
        }

    }

    fun onDeleteImageSelected(uri:Uri){
        _selectedImages.value -= uri
    }

    fun onSendClick(msg:String){
        _currentChats.value += ChatBubble(msg,false)
    }

    fun onOptionSelected(option: String) {
        // 1. Add the user's choice to the chat (isBot must be false here!)
        _currentChats.value = _currentChats.value + ChatBubble(text = option, isBot = false)

        // 2. State Machine Logic to decide the NEXT bot message and state
        when (_currentState.value) {
            is ChatScreenState.Welcome -> {
                // Next question!
                _currentChats.value = _currentChats.value + ChatBubble(text = "Got it. What kind of document is this?", isBot = true)
                // Move to AskDocType (this automatically updates the UI chips)
                _currentState.value = ChatScreenState.AskDocType
            }

            is ChatScreenState.AskDocType -> {
                _currentChats.value = _currentChats.value + ChatBubble(text = "Okay, standard A4 size or keep the original?", isBot = true)
                _currentState.value = ChatScreenState.AskPageOrientation
            }


            is ChatScreenState.AskPageOrientation -> {
                _currentChats.value = _currentChats.value + ChatBubble(text = "Perfect! I'm putting your PDF together now. Give me just a second...", isBot = true)
                _currentState.value = ChatScreenState.Processing

                // NOTE: Later in Phase 5, this is exactly where you will call
                // your buildPdf() coroutine function!
            }

            is ChatScreenState.Processing -> {
                // The user shouldn't be able to trigger this because the Processing state
                // has an empty list of options, meaning no chips are drawn on screen.
            }
        }
    }
}
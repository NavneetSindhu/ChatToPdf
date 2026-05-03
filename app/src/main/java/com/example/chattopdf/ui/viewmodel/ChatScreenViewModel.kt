package com.example.chattopdf.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chattopdf.model.ChatBubble
import com.example.chattopdf.model.ChatScreenState
import com.example.chattopdf.utils.generatePdfFromImages
import com.example.chattopdf.utils.uriToBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sharePdf

class ChatScreenViewModel: ViewModel() {

    private val _currentState = MutableStateFlow<ChatScreenState>(ChatScreenState.Welcome)
    val currentState = _currentState.asStateFlow()

    private var imagesForPdf: List<Uri> = emptyList()
    private val _currentChats = MutableStateFlow<List<ChatBubble>>(
        listOf(ChatBubble("Hi! I'm PDFDidi. Tap the paperclip to select the photos you want to turn into a PDF.", isBot = true))
    )
    val currentChats = _currentChats.asStateFlow()

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages = _selectedImages.asStateFlow()


    fun onImageSelected(uris:List<Uri>){
        if(uris.isNotEmpty()){
            _selectedImages.value += uris
        }

    }

    fun clearSelectedImages(){
        _selectedImages.value = emptyList()
    }

    fun onDeleteImageSelected(uri:Uri){
        _selectedImages.value -= uri
    }



    fun submitUserResponse(userInput: String, context: Context) {

        val currentImages = _selectedImages.value.toList()
        if (currentImages.isNotEmpty()) {
            imagesForPdf = currentImages
            _selectedImages.value = emptyList() // This makes the bottom bar snap shut immediately!
        }

        // 1. Format the user's message based on if they included images or not
        val finalUserText = if (_currentState.value is ChatScreenState.Welcome && _selectedImages.value.isNotEmpty()) {
            if (userInput.isBlank()) "I've attached ${_selectedImages.value.size} image(s)."
            else "Attached ${_selectedImages.value.size} image(s): $userInput"
        } else {
            userInput // Just print exactly what the user typed/tapped
        }

        // Prevent sending absolute blank messages
        if (finalUserText.isBlank()) return

        // 2. Add the user's message to the chat
        _currentChats.value += ChatBubble(
            text = finalUserText,
            isBot = false,
            userImages = currentImages // Pass the list to the bubble
        )

        // 3. State Machine Logic to decide the NEXT bot message
        when (_currentState.value) {
            is ChatScreenState.Welcome -> {
                _currentChats.value += ChatBubble(text = "Got it. What kind of document is this?", isBot = true)
                _currentState.value = ChatScreenState.AskDocType
            }

            is ChatScreenState.AskDocType -> {
                _currentChats.value += ChatBubble(text = "Okay, standard A4 size or keep the original?", isBot = true)
                _currentState.value = ChatScreenState.AskPageOrientation
            }

            is ChatScreenState.AskPageOrientation -> {
                _currentChats.value += ChatBubble(text = "Perfect! I'm putting your PDF together now. Give me just a second...", isBot = true)
                _currentState.value = ChatScreenState.Processing

                viewModelScope.launch {
                    val generatedFile = generatePdfFromImages(context, _selectedImages.value)

                    if (generatedFile != null) {
                        _currentChats.value += ChatBubble("Done! Here is your PDF.", isBot = true, attachment = generatedFile)
                        // Trigger share sheet!
                        sharePdf(context, generatedFile)

                        // Optional: Reset the state back to Welcome so they can make another PDF!
                        _currentState.value = ChatScreenState.Welcome
                        _selectedImages.value = emptyList() // NOW it is safe to clear the images!
                    } else {
                        _currentChats.value += ChatBubble("Oops, something went wrong building the PDF.", isBot = true)
                    }
                }
            }

            is ChatScreenState.Processing -> { /* Do nothing */ }
        }
    }
}
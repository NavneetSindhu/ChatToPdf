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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sharePdf

class ChatScreenViewModel : ViewModel() {

    private val _currentState = MutableStateFlow<ChatScreenState>(ChatScreenState.Welcome)
    val currentState = _currentState.asStateFlow()

    private var imagesForPdf: List<Uri> = emptyList()

    private val _currentChats = MutableStateFlow<List<ChatBubble>>(
        listOf(ChatBubble("Hi! I'm PDFDidi. Tap the paperclip to select the photos you want to turn into a PDF.", isBot = true))
    )
    val currentChats = _currentChats.asStateFlow()

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages = _selectedImages.asStateFlow()

    fun onImageSelected(uris: List<Uri>) {
        if (uris.isNotEmpty()) _selectedImages.value += uris
    }

    fun onDeleteImageSelected(uri: Uri) {
        _selectedImages.value -= uri
    }

    fun submitUserResponse(userInput: String, context: Context) {
        val inputLower = userInput.lowercase()
        val currentImages = _selectedImages.value.toList()

        // 1. Snapshot and clear UI instantly
        if (currentImages.isNotEmpty()) {
            imagesForPdf = currentImages
            _selectedImages.value = emptyList()
        }

        // 2. Format text based on content
        val finalUserText = if (currentImages.isNotEmpty() && userInput.isBlank()) {
            "I've attached ${currentImages.size} image(s)."
        } else {
            userInput
        }

        if (finalUserText.isBlank() && currentImages.isEmpty()) return

        // 3. Add User Bubble immediately
        _currentChats.value += ChatBubble(
            text = finalUserText,
            isBot = false,
            userImages = currentImages
        )

        // 4. Intelligence Logic: Detect Keywords
        val mentionsA4 = inputLower.contains("a4") || inputLower.contains("standard")
        val mentionsOriginal = inputLower.contains("original") || inputLower.contains("actual")
        val mentionsDocType = inputLower.contains("receipt") || inputLower.contains("id") || inputLower.contains("document")

        viewModelScope.launch {
            when {
                // SKIP TO END: User gave all info or we're at the last step
                (_currentState.value is ChatScreenState.AskPageOrientation) || (imagesForPdf.isNotEmpty() && (mentionsA4 || mentionsOriginal)) -> {
                    val sizePref = if (mentionsA4) "A4" else "Original"
                    botResponse("Understood. Using $sizePref size. Putting your PDF together now...")
                    generateAndSharePdf(context, userInput)
                }

                // SKIP TO SIZE: User mentioned doc type but not size
                (_currentState.value is ChatScreenState.AskDocType) || (imagesForPdf.isNotEmpty() && mentionsDocType) -> {
                    botResponse("Got it! Should I use standard A4 size or keep the original dimensions?", ChatScreenState.AskPageOrientation)
                }

                // DEFAULT WELCOME
                _currentState.value is ChatScreenState.Welcome -> {
                    if (imagesForPdf.isEmpty()) {
                        botResponse("Please attach some images first so I can help you!")
                    } else {
                        botResponse("I see those! What kind of document is this? (Receipt, ID, etc.)", ChatScreenState.AskDocType)
                    }
                }
            }
        }
    }

    private suspend fun botResponse(text: String, nextState: ChatScreenState = ChatScreenState.Welcome) {
        delay(600) // Natural "thinking" pause
        _currentChats.value += ChatBubble(text = text, isBot = true)
        _currentState.value = nextState
    }

    private fun generateAndSharePdf(context: Context, userInput: String) {
        _currentState.value = ChatScreenState.Processing

        viewModelScope.launch {
            // Impactful: Use user text as the filename if it's short!
            val fileName = if (userInput.isNotBlank() && userInput.length < 15) userInput else "PDFDidi_Export"

            val generatedFile = generatePdfFromImages(context, imagesForPdf) // You can update your util to accept fileName later

            if (generatedFile != null) {
                botResponse("Done! Here is your PDF.", ChatScreenState.Welcome)
                // Add attachment to the last bot message
                val lastMsg = _currentChats.value.last()
                _currentChats.value = _currentChats.value.dropLast(1) + lastMsg.copy(attachment = generatedFile)

                sharePdf(context, generatedFile)
                imagesForPdf = emptyList()
            } else {
                botResponse("Oops, something went wrong building the PDF.")
            }
        }
    }
}
package com.example.chattopdf.ui.viewmodel

import ChatRepository
import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chattopdf.data.local.AppDatabase
import com.example.chattopdf.data.local.entity.ChatMessage
import com.example.chattopdf.model.ChatBubble
import com.example.chattopdf.model.ChatScreenState
import com.example.chattopdf.utils.generatePdfFromImages
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import sharePdf
import java.io.File

class ChatScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val chatRepository: ChatRepository
    private var currentSessionId: Long = -1

    init {
        val database = AppDatabase.getDatabase(application)
        chatRepository = ChatRepository(database.chatDao(), application)
    }

    // Correct way to expose history to the UI from a ViewModel
    val historySessions = chatRepository.allSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentState = MutableStateFlow<ChatScreenState>(ChatScreenState.Welcome)
    val currentState = _currentState.asStateFlow()

    private val _currentChats = MutableStateFlow<List<ChatBubble>>(
        listOf(ChatBubble(text = "Hi! I'm PDFDidi. Tap the paperclip to select photos for your PDF.", isBot = true))
    )
    val currentChats = _currentChats.asStateFlow()

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages = _selectedImages.asStateFlow()

    private var imagesForPdf: List<Uri> = emptyList()

    private val _currentSessionIdFlow = MutableStateFlow<Long>(-1)
    val currentSessionIdFlow = _currentSessionIdFlow.asStateFlow()

    // Update this in your logic:
    fun loadSession(sessionId: Long) {
        _currentSessionIdFlow.value = sessionId // Track current ID
        currentSessionId = sessionId
        // ... rest of loading code
    }

    fun startNewChat() {
        _currentSessionIdFlow.value = -1 // Reset for new chat
        currentSessionId = -1L
        // ... rest of new chat code
    }

    // Extension function to convert Entity -> UI Model
    private fun ChatMessage.toChatBubble(): ChatBubble {
        return ChatBubble(
            messageId = this.messageId,
            sessionId = this.parentSessionId,
            text = this.text,
            isBot = this.isBot,
            userImages = this.imagePaths.map { Uri.parse(it) }
        )
    }

    fun onImageSelected(uris: List<Uri>) {
        if (uris.isNotEmpty()) _selectedImages.value += uris
    }

    fun onDeleteImageSelected(uri: Uri) {
        _selectedImages.value -= uri
    }

    fun submitUserResponse(userInput: String, context: Context) {
        val inputLower = userInput.lowercase()
        val urisToSave = _selectedImages.value.toList()

        if (urisToSave.isNotEmpty()) {
            imagesForPdf = urisToSave
            _selectedImages.value = emptyList()
        }

        val finalUserText = if (urisToSave.isNotEmpty() && userInput.isBlank()) {
            "I've attached ${urisToSave.size} image(s)."
        } else {
            userInput
        }

        if (finalUserText.isBlank() && urisToSave.isEmpty()) return

        viewModelScope.launch {
            if (currentSessionId == -1L) {
                val title = if (userInput.isNotBlank()) userInput.take(20) else "New PDF Project"
                currentSessionId = chatRepository.createNewSession(title)
            }

            chatRepository.saveUserMessage(currentSessionId, finalUserText, urisToSave)

            _currentChats.value += ChatBubble(
                sessionId = currentSessionId,
                text = finalUserText,
                isBot = false,
                userImages = urisToSave
            )

            processBotLogic(inputLower, context, userInput)
        }
    }

    private suspend fun processBotLogic(inputLower: String, context: Context, userInput: String) {
        val mentionsA4 = inputLower.contains("a4") || inputLower.contains("standard")
        val mentionsOriginal = inputLower.contains("original") || inputLower.contains("actual")
        val mentionsDocType = inputLower.contains("receipt") || inputLower.contains("id") || inputLower.contains("doc")

        when {
            (_currentState.value is ChatScreenState.AskPageOrientation) || (imagesForPdf.isNotEmpty() && (mentionsA4 || mentionsOriginal)) -> {
                botResponse("Understood. Generating your PDF now...")
                generateAndSharePdf(context, userInput)
            }
            (_currentState.value is ChatScreenState.AskDocType) || (imagesForPdf.isNotEmpty() && mentionsDocType) -> {
                botResponse(
                    text = "Got it! Should I use standard A4 size or keep original dimensions?",
                    nextState = ChatScreenState.AskPageOrientation,
                    suggestions = listOf("A4 Size", "Original Size")
                )
            }
            else -> {
                if (imagesForPdf.isEmpty() && _currentState.value is ChatScreenState.Welcome) {
                    botResponse("Please attach some images first so I can help you!")
                } else {
                    botResponse(
                        text = "I see those! What kind of document is this?",
                        nextState = ChatScreenState.AskDocType,
                        suggestions = listOf("Receipt", "ID Card", "Document")
                    )
                }
            }
        }
    }

    private suspend fun botResponse(
        text: String,
        nextState: ChatScreenState = ChatScreenState.Welcome,
        suggestions: List<String> = emptyList()
    ) {
        delay(700)
        _currentChats.value += ChatBubble(
            sessionId = currentSessionId,
            text = text,
            isBot = true,
            suggestions = suggestions
        )
        _currentState.value = nextState
    }

    private fun generateAndSharePdf(context: Context, userInput: String) {
        _currentState.value = ChatScreenState.Processing
        viewModelScope.launch {
            val generatedFile = generatePdfFromImages(context, imagesForPdf)
            if (generatedFile != null) {
                botResponse("Done! Here is your PDF. You can share it or keep editing.")
                val lastMsg = _currentChats.value.last()
                _currentChats.value = _currentChats.value.dropLast(1) + lastMsg.copy(
                    attachment = generatedFile,
                    suggestions = listOf("Start New", "Edit This")
                )
                sharePdf(context, generatedFile)
                imagesForPdf = emptyList()
            } else {
                botResponse("Oops, something went wrong building the PDF.")
            }
        }
    }
}
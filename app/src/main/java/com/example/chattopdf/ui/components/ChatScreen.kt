package com.example.chattopdf.ui.components

import BotBubble
import PdfBubble
import android.R
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.chattopdf.ui.viewmodel.ChatScreenViewModel
import kotlinx.coroutines.delay
import viewPdf

// Theme Colors
val ProfileIconBg = Color(0xFF080A09)
val ScreenBackground = Color(0xFFF8F5EB)
val InputBorderColor = Color(0xFFD6D3C4)

@Composable
fun ChatScreen(
    paddingValues: PaddingValues,
    chatScreenViewModel: ChatScreenViewModel = viewModel()
) {
    val context = LocalContext.current
    val chatList by chatScreenViewModel.currentChats.collectAsState()
    val selectedImages by chatScreenViewModel.selectedImages.collectAsState()
    val listState = rememberLazyListState()

    // UI Local State
    var expandedImageUri by remember { mutableStateOf<Uri?>(null) }
    var messageText by remember { mutableStateOf("") }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(chatList.size) {
        if (chatList.isNotEmpty()) {
            delay(200)
            listState.animateScrollToItem(chatList.size - 1)
        }
    }

    // Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        chatScreenViewModel.onImageSelected(uris)
    }

    Scaffold(
        modifier = Modifier.padding(paddingValues),
        topBar = { ChatTopBar() },
        bottomBar = {
            // ActionChoices are now handled INSIDE BotBubble for a cleaner vibe
            ChatBottomBar(
                text = messageText,
                onTextChanged = { messageText = it },
                onSendClicked = {
                    if (messageText.isNotBlank() || selectedImages.isNotEmpty()) {
                        chatScreenViewModel.submitUserResponse(userInput = messageText, context = context)
                        messageText = ""
                    }
                },
                onAttachmentClicked = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                selectedImages = selectedImages,
                onRemoveImage = { uriToRemove ->
                    chatScreenViewModel.onDeleteImageSelected(uriToRemove)
                }
            )
        },
        containerColor = ScreenBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(chatList) { msg ->
                    if (msg.isBot) {
                        if (msg.attachment != null) {
                            PdfBubble(
                                chatBubble = msg,
                                onClick = { file -> viewPdf(context, file) }
                            )
                        } else {
                            // Smart Bot Bubble with contextual suggestions
                            BotBubble(
                                chatBubble = msg,
                                onOptionSelected = { choice ->
                                    chatScreenViewModel.submitUserResponse(userInput = choice, context = context)
                                }
                            )
                        }
                    } else {
                        UserBubble(
                            chatBubble = msg,
                            onImageClick = { clickedUri -> expandedImageUri = clickedUri }
                        )
                    }
                }
            }
        }
    }

    // Full-screen Image Viewer
    if (expandedImageUri != null) {
        Dialog(
            onDismissRequest = { expandedImageUri = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { expandedImageUri = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = expandedImageUri,
                    contentDescription = "Full View",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ChatTopBar() {
    Surface(shadowElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ProfileIconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "PDFDidi",
                    color = PrimaryDarkGreen,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            IconButton(onClick = { /* History or Settings */ }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = PrimaryDarkGreen)
            }
        }
    }
}

@Composable
fun ChatBottomBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onAttachmentClicked: () -> Unit,
    selectedImages: List<Uri>,
    onRemoveImage: (Uri) -> Unit
) {
    Surface(
        color = ScreenBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            if (selectedImages.isNotEmpty()) {
                ImagePreviewRow(selectedImages, onRemoveImage)
            }

            Row(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 4.dp)
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(28.dp))
                    .border(1.dp, InputBorderColor, RoundedCornerShape(28.dp))
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onAttachmentClicked) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_menu_add),
                        contentDescription = "Attach",
                        tint = Color.Gray
                    )
                }

                BasicTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                    cursorBrush = SolidColor(PrimaryDarkGreen),
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            Text("Ask Didi...", color = Color.Gray, fontSize = 16.sp)
                        }
                        innerTextField()
                    }
                )

                Surface(
                    modifier = Modifier.size(42.dp).clip(CircleShape),
                    color = if (text.isNotBlank() || selectedImages.isNotEmpty()) PrimaryDarkGreen else Color.LightGray
                ) {
                    IconButton(
                        onClick = onSendClicked,
                        enabled = text.isNotBlank() || selectedImages.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
package com.example.chattopdf.ui.components


import BotBubble
import PdfBubble
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.launch
import viewPdf

// Define the colors or ensure they are imported from your Theme/MainActivity
val ProfileIconBg = Color(0xFF080A09)
val ScreenBackground = Color(0xFFE7E3C3)
val InputBorderColor = Color(0xFF343131)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    paddingValues: PaddingValues,
    chatScreenViewModel: ChatScreenViewModel = viewModel(),
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val chatList by chatScreenViewModel.currentChats.collectAsState()
    val selectedImages by chatScreenViewModel.selectedImages.collectAsState()
    val historySessions by chatScreenViewModel.historySessions.collectAsState()

    // We need to track the current ID in the UI to highlight the active session in the drawer
    // You can also add a StateFlow in the ViewModel for this for better architecture
    val currentSessionId by chatScreenViewModel.currentSessionIdFlow.collectAsState()

    val listState = rememberLazyListState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Bottom sheet variables
    val sheetState = rememberModalBottomSheetState()
    var showSettingsSheet by remember { mutableStateOf(false) }


    var expandedImageUri by remember { mutableStateOf<Uri?>(null) }
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(chatList.size) {
        if (chatList.isNotEmpty()) {
            delay(100)
            listState.animateScrollToItem(chatList.size - 1)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        chatScreenViewModel.onImageSelected(uris)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = ScreenBackground,
                modifier = Modifier.fillMaxWidth(.9f)
            ) {
                Spacer(Modifier.height(12.dp))

                Text(
                    "History",
                    modifier = Modifier.padding(16.dp),
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = PrimaryDarkGreen
                    )
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = InputBorderColor
                )

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        NavigationDrawerItem(
                            label = { Text("New Chat", color = PrimaryDarkGreen) },
                            selected = false,
                            onClick = {
                                chatScreenViewModel.startNewChat()
                                scope.launch { drawerState.close() }
                            },
                            icon = { Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryDarkGreen) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }

                    items(historySessions) { session ->
                        NavigationDrawerItem(
                            label = { Text(session.title) },
                            // Correctly highlighting the active project
                            selected = session.sessionId == currentSessionId,
                            onClick = {
                                chatScreenViewModel.loadSession(session.sessionId)
                                scope.launch { drawerState.close() }
                            },
                            badge = {
                                IconButton(onClick = {
                                    // Logic to select this specific session for the bottom sheet
                                    // and then show the sheet
                                    showSettingsSheet = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Options",
                                        tint = PrimaryDarkGreen
                                    )
                                }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.padding(paddingValues),
            topBar = {
                ChatTopBar(onMenuClick = {
                    scope.launch { drawerState.open() }
                },onSettingsClick = { onNavigateToSettings() })
            },
            bottomBar = {
                ChatBottomBar(
                    text = messageText,
                    onTextChanged = { messageText = it },
                    onSendClicked = {
                        if (messageText.isNotBlank() || selectedImages.isNotEmpty()) {
                            chatScreenViewModel.submitUserResponse(
                                userInput = messageText,
                                context = context
                            )
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
                                BotBubble(
                                    chatBubble = msg,
                                    onOptionSelected = { choice ->
                                        chatScreenViewModel.submitUserResponse(
                                            userInput = choice,
                                            context = context
                                        )
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
    }

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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
        }
    }
    // ... inside ChatScreen at the very bottom ...

    GenericBottomSheet(
        showSheet = showSettingsSheet,
        sheetState = sheetState,
        onDismiss = { showSettingsSheet = false }
    ) {
        // Content of the Bottom Sheet
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 24.dp) // Space for system nav bar
        ) {
            Text(
                text = "Project Options",
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ListItem(
                headlineContent = { Text("Rename Project") },
                leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                modifier = Modifier.clickable { /* Rename Logic */ }
            )

            ListItem(
                headlineContent = { Text("Delete Project", color = Color.Red) },
                leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                modifier = Modifier.clickable {
                    // chatScreenViewModel.deleteSession(currentSessionId)
                    showSettingsSheet = false
                }
            )
        }
    }
}

// ... Rest of your ChatTopBar and ChatBottomBar remain the same

@Composable
fun ChatTopBar(onMenuClick: () -> Unit,onSettingsClick: () -> Unit) {
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
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = PrimaryDarkGreen
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PDFDidi",
                    color = PrimaryDarkGreen,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = PrimaryDarkGreen
                )
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
                        painter = painterResource(id = android.R.drawable.ic_menu_add),
                        contentDescription = "Attach",
                        tint = Color.Gray
                    )
                }

                BasicTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
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
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape),
                    color = if (text.isNotBlank() || selectedImages.isNotEmpty())
                        PrimaryDarkGreen else Color.LightGray
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
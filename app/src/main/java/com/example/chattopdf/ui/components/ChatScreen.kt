package com.example.chattopdf.ui.components

import BotBubble
import PdfBubble
import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
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
import com.example.chattopdf.utils.ThemeManager
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import viewPdf

// Defined here so all components can access the primary accent color
//val PrimaryDarkGreen = Color(0xFF156A4C)

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    paddingValues: PaddingValues,
    chatScreenViewModel: ChatScreenViewModel = viewModel(),
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current as Activity
    val chatList by chatScreenViewModel.currentChats.collectAsState()
    val selectedImages by chatScreenViewModel.selectedImages.collectAsState()
    val historySessions by chatScreenViewModel.historySessions.collectAsState()

    val currentSessionId by chatScreenViewModel.currentSessionIdFlow.collectAsState()

    val listState = rememberLazyListState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Bottom sheet variables
    val sheetState = rememberModalBottomSheetState()
    var showSettingsSheet by remember { mutableStateOf(false) }

    // NEW: Track WHICH project the user clicked the 3-dots on
    var sessionForOptions by remember { mutableStateOf<Long?>(null) }

    var expandedImageUri by remember { mutableStateOf<Uri?>(null) }
    var messageText by remember { mutableStateOf("") }

    // DataStore Theme
    val themeManager = remember { ThemeManager(context) }
    val screenBackground by themeManager.screenBgFlow.collectAsState(initial = ThemeManager.DefaultScreenBg)
    val profileIconBg by themeManager.profileBgFlow.collectAsState(initial = ThemeManager.DefaultProfileBg)
    val inputBorderColor by themeManager.borderColorFlow.collectAsState(initial = ThemeManager.DefaultBorder)

    LaunchedEffect(chatList.size) {
        if (chatList.isNotEmpty()) {
            delay(100)
            listState.animateScrollToItem(chatList.size - 1)
        }
    }

    val scannerOptions = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(100)
            .setResultFormats(RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
    }

    val scannerClient = remember { GmsDocumentScanning.getClient(scannerOptions) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            val scannedUris = scanResult?.pages?.map { it.imageUri } ?: emptyList()
            if (scannedUris.isNotEmpty()) {
                chatScreenViewModel.onImageSelected(scannedUris)
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = screenBackground,
                modifier = Modifier.fillMaxWidth(.85f) // Slightly narrower looks better
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
                    color = inputBorderColor
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
                            selected = session.sessionId == currentSessionId,
                            onClick = {
                                chatScreenViewModel.loadSession(session.sessionId)
                                scope.launch { drawerState.close() }
                            },
                            badge = {
                                IconButton(onClick = {
                                    // Save the ID before opening the sheet!
                                    sessionForOptions = session.sessionId
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
                ChatTopBar(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onSettingsClick = { onNavigateToSettings() }
                )
            },
            bottomBar = {
                // Passed the dynamic colors here!
                ChatBottomBar(
                    text = messageText,
                    onTextChanged = { messageText = it },
                    screenBackground = screenBackground,
                    inputBorderColor = inputBorderColor,
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
                        scannerClient.getStartScanIntent(context)
                            .addOnSuccessListener { intentSender ->
                                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                            }
                    },
                    selectedImages = selectedImages,
                    onRemoveImage = { uriToRemove ->
                        chatScreenViewModel.onDeleteImageSelected(uriToRemove)
                    }
                )
            },
            containerColor = screenBackground
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

    // Image Preview Dialog
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

    // Bottom Sheet for Project Options
    GenericBottomSheet(
        showSheet = showSettingsSheet,
        sheetState = sheetState,
        onDismiss = { showSettingsSheet = false }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Project Options",
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ListItem(
                headlineContent = { Text("Rename Project") },
                leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                modifier = Modifier.clickable {
                    // Future Rename Logic using sessionForOptions
                    showSettingsSheet = false
                }
            )

            ListItem(
                headlineContent = { Text("Delete Project", color = Color.Red) },
                leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                modifier = Modifier.clickable {
                    // We now know WHICH session to delete!
                    sessionForOptions?.let { id ->
                        // chatScreenViewModel.deleteSession(id)
                    }
                    showSettingsSheet = false
                }
            )
        }
    }
}

@Composable
fun ChatTopBar(onMenuClick: () -> Unit, onSettingsClick: () -> Unit) {
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
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = PrimaryDarkGreen)
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
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = PrimaryDarkGreen)
            }
        }
    }
}

@Composable
fun ChatBottomBar(
    text: String,
    onTextChanged: (String) -> Unit,
    screenBackground: Color,   // Added Dynamic Color Parameter
    inputBorderColor: Color,   // Added Dynamic Color Parameter
    onSendClicked: () -> Unit,
    onAttachmentClicked: () -> Unit,
    selectedImages: List<Uri>,
    onRemoveImage: (Uri) -> Unit
) {
    Surface(
        color = screenBackground,
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
                    .border(1.dp, inputBorderColor, RoundedCornerShape(28.dp))
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
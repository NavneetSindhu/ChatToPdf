import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.AccountCircle
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.chattopdf.model.ChatBubble
import com.example.chattopdf.ui.components.ActionChoices
import com.example.chattopdf.ui.components.ImagePreviewRow
import com.example.chattopdf.ui.components.UserBubble
import com.example.chattopdf.ui.viewmodel.ChatScreenViewModel
import kotlinx.coroutines.delay

// Define the colors extracted from your design
val PrimaryDarkGreen = Color(0xFF156A4C)
val ProfileIconBg = Color(0xFF080A09)
val ScreenBackground = Color(0xFFF8F5EB)
val InputBorderColor = Color(0xFFD6D3C4)

@Composable
fun ChatScreen(paddingValues: PaddingValues,chatScreenViewModel: ChatScreenViewModel= viewModel()) {
    // Hoisted state for the text input
    var expandedImageUri by remember { mutableStateOf<Uri?>(null) }
    var messageText by remember { mutableStateOf("") }
    val chatList by chatScreenViewModel.currentChats.collectAsState()
    val currentState by chatScreenViewModel.currentState.collectAsState()
    val selectedImages by chatScreenViewModel.selectedImages.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(chatList.size) {
                if (chatList.isNotEmpty()) {
                    // Animate scroll to the very last item
                    delay(200)
                    listState.animateScrollToItem(chatList.size - 1)
                }
            }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        chatScreenViewModel.onImageSelected(uris)

    }


    Scaffold(
        modifier = Modifier.padding(paddingValues),
        topBar = {
            ChatTopBar()
        },
        bottomBar = {
            Column {
                // Only show this row if there are actual options to display
                // (We will hook this up to the ViewModel's state next)
                if (currentState.options.isNotEmpty()) {
                    ActionChoices(
                        options = currentState.options,
                        onOptionSelected = { choice ->
                            chatScreenViewModel.submitUserResponse(context = context, userInput = choice)
                        }
                    )
                }
                ChatBottomBar(
                    text = messageText,
                    onTextChanged = { messageText = it },
                    onSendClicked = {
                        chatScreenViewModel.submitUserResponse(userInput = messageText, context = context)
                        messageText = ""
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
            }
        },
        containerColor = ScreenBackground
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .imePadding(),
                state = listState,
                reverseLayout = false,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(chatList) { msg ->
                    if (msg.isBot) {
                        // If there's an attachment, show the PDF Bubble!
                        if (msg.attachment != null) {
                            PdfBubble(
                                chatBubble = msg,
                                onClick = { file ->
                                    viewPdf(context,file)
                                }
                            )
                        } else {
                            BotBubble(msg) // Normal text bot message
                        }
                    } else {
                        UserBubble(msg,onImageClick = { clickedUri -> expandedImageUri = clickedUri }) // Normal user message
                    }
                }
            }

        }
    }
    if (expandedImageUri != null) {
        Dialog(
            onDismissRequest = { expandedImageUri = null },
            properties = DialogProperties(usePlatformDefaultWidth = false) // Allows full screen
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { expandedImageUri = null }, // Tap anywhere to close
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = expandedImageUri,
                    contentDescription = "Expanded Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ChatTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Profile Icon Placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ProfileIconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title
            Text(
                text = "PDFDidi",
                color = PrimaryDarkGreen,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Settings Icon
        IconButton(onClick = { /* Handle settings */ }) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = PrimaryDarkGreen
            )
        }
    }
}

@Composable
fun ChatBottomBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onAttachmentClicked:()-> Unit,
    selectedImages: List<Uri>,
    onRemoveImage: (Uri) -> Unit
) {
    // We wrap the input in a Surface to match the screen's background color
    Surface(
        color = ScreenBackground,
        modifier = Modifier.fillMaxWidth()
    ) {

        Column {
            if(selectedImages.isNotEmpty()){
                ImagePreviewRow(selectedImages,onRemoveImage)
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(50))
                    .border(1.dp, InputBorderColor, RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Attachment Button
                IconButton(onClick = {
                    onAttachmentClicked()
                }) {
                    // Using a generic clip icon. You can replace with painterResource(id = R.drawable.ic_attachment)
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_add),
                        contentDescription = "Attach file",
                        tint = Color.Gray
                    )
                }

                // Text Input Field
                BasicTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(PrimaryDarkGreen),
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            Text(
                                text = "Type a message...",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                )

                // Send Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrimaryDarkGreen),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onSendClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send message",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

    }
}
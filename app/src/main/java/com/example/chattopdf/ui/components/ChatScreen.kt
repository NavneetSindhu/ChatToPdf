import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chattopdf.model.ChatBubble
import com.example.chattopdf.ui.components.ActionChoices
import com.example.chattopdf.ui.components.ImagePreviewRow
import com.example.chattopdf.ui.components.UserBubble
import com.example.chattopdf.ui.viewmodel.ChatScreenViewModel

// Define the colors extracted from your design
val PrimaryDarkGreen = Color(0xFF144D3A)
val ProfileIconBg = Color(0xFF90C2B2)
val ScreenBackground = Color(0xFFF8F5EB)
val InputBorderColor = Color(0xFFD6D3C4)

@Composable
fun ChatScreen(paddingValues: PaddingValues,chatScreenViewModel: ChatScreenViewModel= ChatScreenViewModel()) {
    // Hoisted state for the text input
    var messageText by remember { mutableStateOf("") }
    val chatList by chatScreenViewModel.currentChats.collectAsState()
    val currentState by chatScreenViewModel.currentState.collectAsState()
    val selectedImages by chatScreenViewModel.selectedImages.collectAsState()
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
                            chatScreenViewModel.onOptionSelected(choice)
                        }
                    )
                }
                ChatBottomBar(
                    text = messageText,
                    onTextChanged = { messageText = it },
                    onSendClicked = {
                        if (messageText.isNotBlank()) {
                            chatScreenViewModel.onOptionSelected(messageText)
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
                modifier = Modifier.fillMaxSize(),
                reverseLayout = false,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(chatList){ msg ->
                    if(msg.isBot){
                        BotBubble(msg)
                    }else UserBubble(msg)

                }
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
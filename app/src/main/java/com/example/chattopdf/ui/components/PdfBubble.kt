import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.chattopdf.model.ChatBubble
import com.example.chattopdf.ui.components.BotBubbleColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PdfBubble(chatBubble: ChatBubble, onClick: (File) -> Unit) {
    // 1. Start with a null image
    var preview by remember { mutableStateOf<Bitmap?>(null) }

    // 2. Fetch the image in the background so the UI doesn't freeze!
    LaunchedEffect(chatBubble.attachment) {
        if (chatBubble.attachment != null) {
            withContext(Dispatchers.IO) {
                preview = getPdfThumbnail(chatBubble.attachment)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            color = BotBubbleColor,
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            modifier = Modifier.padding(end = 48.dp),
            // 3. Attach the click listener here!
            onClick = {
                chatBubble.attachment?.let { file -> onClick(file) }
            }
        ) {
            Column {
                Text(
                    text = chatBubble.text,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )

                // 4. Safely unwrap and display the image once it finishes loading
                preview?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "PDF Thumbnail",
                        contentScale = ContentScale.Crop, // Keeps the image looking neat
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp) // Give it a fixed height
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(8.dp)) // Round the inner image corners
                    )
                }
            }
        }
    }
}
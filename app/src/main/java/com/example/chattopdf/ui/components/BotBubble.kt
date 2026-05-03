import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.chattopdf.model.ChatBubble
import com.example.chattopdf.ui.components.BotBubbleColor

@Composable
fun BotBubble(chatBubble: ChatBubble) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterStart // Pushes the bubble to the left
    ) {
        Surface(
            color = BotBubbleColor,
            // Asymmetric corners: smaller corner on the top left where it "points" to the bot profile
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            // Padding to ensure the bubble doesn't stretch all the way across the screen
            modifier = Modifier.padding(end = 48.dp)
        ) {
            Text(
                text = chatBubble.text,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.chattopdf.model.ChatBubble
import com.example.chattopdf.ui.components.ActionChoices

// Colors from your design
val BotBubbleColor = Color(0xFFF0EDE4)

@Composable
fun BotBubble(
    chatBubble: ChatBubble,
    onOptionSelected: (String) -> Unit // Pass the click handler up to the ViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // The Message Bubble
        Surface(
            color = BotBubbleColor,
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            modifier = Modifier.padding(end = 48.dp)
        ) {
            Text(
                text = chatBubble.text,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }

        // The Action Chips (Only show if suggestions list is not empty)
        if (chatBubble.suggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            ActionChoices(
                options = chatBubble.suggestions,
                onOptionSelected = onOptionSelected
            )
        }
    }
}
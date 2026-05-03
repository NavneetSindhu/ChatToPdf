package com.example.chattopdf.ui.components

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

// Colors based on your initial design
val UserBubbleColor = Color(0xFF144D3A) // Dark Green
val BotBubbleColor = Color(0xFFEAE7D9)  // Light Beige

@Composable
fun UserBubble(chatBubble: ChatBubble) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterEnd // Pushes the bubble to the right
    ) {
        Surface(
            color = UserBubbleColor,
            // Asymmetric corners: smaller corner on the bottom right where it "points" to the user
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 4.dp
            ),
            // Padding to ensure the bubble doesn't stretch all the way across the screen
            modifier = Modifier.padding(start = 48.dp)
        ) {
            Text(
                text = chatBubble.text,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}


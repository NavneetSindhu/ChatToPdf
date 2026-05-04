package com.example.chattopdf.ui.components

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.chattopdf.model.ChatBubble

// Colors based on your initial design
val UserBubbleColor = Color(0xFF144D3A) // Dark Green
val BotBubbleColor = Color(0xFFEAE7D9)  // Light Beige

@Composable
fun UserBubble(chatBubble: ChatBubble, onImageClick: (Uri) -> Unit) {
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
            Column(modifier = Modifier.padding(8.dp)) {

                // 1. Draw the images if they exist
                if (chatBubble.userImages.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = if (chatBubble.text.isNotBlank()) 8.dp else 0.dp)
                    ) {
                        items(chatBubble.userImages) { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = "Attached Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(100.dp) // Nice thumbnail size
                                    .clip(RoundedCornerShape(8.dp)) // Round the image corners slightly
                                    .clickable { onImageClick(uri) } // Triggers the full screen viewer
                            )
                        }
                    }
                }

                // 2. Draw the text
                if (chatBubble.text.isNotBlank()) {
                    Text(
                        text = chatBubble.text,
                        color = Color.White, // White text to contrast with your Dark Green bubble
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
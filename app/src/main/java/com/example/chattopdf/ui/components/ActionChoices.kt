package com.example.chattopdf.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Assuming you have this defined somewhere accessible, or just redefine it here
val PrimaryDarkGreen = Color(0xFF144D3A)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActionChoices(
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    // FlowRow automatically wraps items to the next line
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            Surface(
                onClick = { onOptionSelected(option) },
                shape = RoundedCornerShape(50),
                color = Color.Transparent,
                border = BorderStroke(1.dp, PrimaryDarkGreen)
            ) {
                Text(
                    text = option,
                    color = PrimaryDarkGreen,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}
package com.pocketdev.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier
) {
    val annotatedString = buildAnnotatedString {
        var currentIndex = 0
        val regex = Regex("\\*\\*(.*?)\\*\\*|`(.*?)`")
        
        val matches = regex.findAll(text)
        
        for (match in matches) {
            if (match.range.first < currentIndex) continue
            
            append(text.substring(currentIndex, match.range.first))
            
            if (match.groups[1] != null) {
                // Bold
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.groups[1]!!.value)
                }
            } else if (match.groups[2] != null) {
                // Code
                withStyle(style = SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = MaterialTheme.colorScheme.surfaceVariant,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )) {
                    append(match.groups[2]!!.value)
                }
            }
            
            currentIndex = match.range.last + 1
        }
        
        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
    
    Text(
        text = annotatedString,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium
    )
}

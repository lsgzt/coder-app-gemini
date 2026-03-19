package com.pocketdev.app.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketdev.app.ui.theme.PocketDevTheme
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PocketDevTheme(darkTheme = true) {
                SplashScreen(onFinished = {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                })
            }
        }
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
        delay(2000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1117), Color(0xFF1A1A2E))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "📱",
                    fontSize = 80.sp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "PocketDev",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4FC3F7)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Code Anywhere, Learn Everything",
                    fontSize = 14.sp,
                    color = Color(0xFF90A4AE)
                )
                Spacer(Modifier.height(48.dp))
                CircularProgressIndicator(
                    color = Color(0xFF4FC3F7),
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

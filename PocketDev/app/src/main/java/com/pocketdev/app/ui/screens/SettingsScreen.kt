package com.pocketdev.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.pocketdev.app.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val theme by viewModel.theme.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val tabSize by viewModel.tabSize.collectAsState()
    val autoSave by viewModel.autoSave.collectAsState()
    val autocomplete by viewModel.autocomplete.collectAsState()
    val ghostSuggestions by viewModel.ghostSuggestions.collectAsState()
    val lineNumbers by viewModel.lineNumbers.collectAsState()
    val wordWrap by viewModel.wordWrap.collectAsState()
    val apiKeyState by viewModel.apiKeyState.collectAsState()
    val aiModel by viewModel.aiModel.collectAsState()

    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showApiKeyInfo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // AI Section
            SettingsSectionHeader("🤖 AI Integration (Groq)")

            // API Key status card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = when (apiKeyState) {
                    is SettingsViewModel.ApiKeyState.Set ->
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    is SettingsViewModel.ApiKeyState.Error ->
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    else -> CardDefaults.cardColors()
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        when (apiKeyState) {
                            is SettingsViewModel.ApiKeyState.Set -> Icons.Default.CheckCircle
                            is SettingsViewModel.ApiKeyState.Error -> Icons.Default.Error
                            else -> Icons.Default.Key
                        },
                        contentDescription = null,
                        tint = when (apiKeyState) {
                            is SettingsViewModel.ApiKeyState.Set -> MaterialTheme.colorScheme.secondary
                            is SettingsViewModel.ApiKeyState.Error -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (apiKeyState) {
                                is SettingsViewModel.ApiKeyState.Set -> "API Key Configured"
                                is SettingsViewModel.ApiKeyState.Error -> "Invalid API Key"
                                else -> "No API Key Set"
                            },
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = when (apiKeyState) {
                                is SettingsViewModel.ApiKeyState.Set ->
                                    (apiKeyState as SettingsViewModel.ApiKeyState.Set).maskedKey
                                is SettingsViewModel.ApiKeyState.Error ->
                                    (apiKeyState as SettingsViewModel.ApiKeyState.Error).message
                                else -> "Required for AI features (Fix Bug, Explain, Improve)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showApiKeyDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (apiKeyState is SettingsViewModel.ApiKeyState.Set) "Change Key" else "Set API Key")
                    }
                    if (apiKeyState is SettingsViewModel.ApiKeyState.Set) {
                        OutlinedButton(
                            onClick = { viewModel.clearApiKey() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Remove")
                        }
                    }
                }
            }

            SettingsItem(
                icon = Icons.Default.Info,
                title = "How to get Groq API Key",
                subtitle = "Free API key from console.groq.com",
                onClick = { showApiKeyInfo = true }
            )

            // AI Model Selection
            var modelText by remember(aiModel) { mutableStateOf(aiModel) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Memory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("AI Model", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Enter exact model name from console.groq.com/docs/models",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = modelText,
                        onValueChange = { modelText = it },
                        label = { Text("Model Name") },
                        placeholder = { Text("e.g. llama-3.3-70b-versatile") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            if (modelText != aiModel) {
                                IconButton(onClick = {
                                    if (modelText.isNotBlank()) {
                                        viewModel.setAiModel(modelText.trim())
                                    }
                                }) {
                                    Icon(Icons.Default.Check, "Save model", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "Visit console.groq.com/docs/models for available model names",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Editor Section
            SettingsSectionHeader("✏️ Editor")

            // Theme selector
            SettingsItemWithContent(
                icon = Icons.Default.Palette,
                title = "Theme",
                subtitle = when (theme) {
                    "light" -> "Light Mode"
                    "auto" -> "Follow System"
                    else -> "Dark Mode"
                }
            ) {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expanded = true }) {
                        Text(
                            when (theme) {
                                "light" -> "☀️ Light"
                                "auto" -> "🔄 Auto"
                                else -> "🌙 Dark"
                            }
                        )
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("dark" to "🌙 Dark", "light" to "☀️ Light", "auto" to "🔄 System")
                            .forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = { viewModel.setTheme(value); expanded = false },
                                    leadingIcon = if (theme == value) {
                                        { Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary) }
                                    } else null
                                )
                            }
                    }
                }
            }

            // Font size slider
            SettingsItemWithContent(
                icon = Icons.Default.TextFields,
                title = "Font Size",
                subtitle = "${fontSize}sp"
            ) {
                Slider(
                    value = fontSize.toFloat(),
                    onValueChange = { viewModel.setFontSize(it.toInt()) },
                    valueRange = 10f..22f,
                    steps = 11,
                    modifier = Modifier.width(140.dp)
                )
            }

            // Tab size
            SettingsItemWithContent(
                icon = Icons.Default.SpaceBar,
                title = "Tab Size",
                subtitle = "$tabSize spaces"
            ) {
                Row {
                    listOf(2, 4, 8).forEach { size ->
                        FilterChip(
                            selected = tabSize == size,
                            onClick = { viewModel.setTabSize(size) },
                            label = { Text("$size") },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
            }

            SettingsSwitchItem(
                icon = Icons.Default.FormatListNumbered,
                title = "Line Numbers",
                subtitle = "Show line numbers in editor",
                checked = lineNumbers,
                onCheckedChange = viewModel::setLineNumbers
            )

            SettingsSwitchItem(
                icon = Icons.Default.WrapText,
                title = "Word Wrap",
                subtitle = "Wrap long lines to fit screen",
                checked = wordWrap,
                onCheckedChange = viewModel::setWordWrap
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Features Section
            SettingsSectionHeader("⚙️ Features")

            SettingsSwitchItem(
                icon = Icons.Default.Save,
                title = "Auto Save",
                subtitle = "Save changes automatically every 30 seconds",
                checked = autoSave,
                onCheckedChange = viewModel::setAutoSave
            )

            SettingsSwitchItem(
                icon = Icons.Default.AutoAwesome,
                title = "Autocomplete",
                subtitle = "Show code completion suggestions",
                checked = autocomplete,
                onCheckedChange = viewModel::setAutocomplete
            )

            SettingsSwitchItem(
                icon = Icons.Default.Psychology,
                title = "AI Ghost Suggestions",
                subtitle = "Show inline AI code suggestions",
                checked = ghostSuggestions,
                onCheckedChange = viewModel::setGhostSuggestions
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // About Section
            SettingsSectionHeader("ℹ️ About")

            SettingsItem(
                icon = Icons.Default.Info,
                title = "PocketDev",
                subtitle = "Version 1.0.0 — Mobile coding for students"
            )

            SettingsItem(
                icon = Icons.Default.Code,
                title = "Supported Languages",
                subtitle = "Python, JavaScript, HTML, CSS, Java, C++, Kotlin, JSON"
            )

            SettingsItem(
                icon = Icons.Default.PlayCircle,
                title = "Execution Engines",
                subtitle = "Python: Chaquopy • JavaScript: Rhino • HTML: WebView"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Reset
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedButton(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.RestartAlt, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Reset to Defaults")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // API Key Dialog
    if (showApiKeyDialog) {
        ApiKeyDialog(
            onSave = { key ->
                viewModel.setApiKey(key)
                showApiKeyDialog = false
            },
            onDismiss = { showApiKeyDialog = false }
        )
    }

    // Reset dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = { Icon(Icons.Default.RestartAlt, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Reset Settings?") },
            text = { Text("This will reset all settings to their defaults. Your projects and API key will not be affected.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetToDefaults()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }

    // API Key info dialog
    if (showApiKeyInfo) {
        AlertDialog(
            onDismissRequest = { showApiKeyInfo = false },
            icon = { Icon(Icons.Default.Key, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Getting a Groq API Key") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Follow these steps to get your free Groq API key:")
                    Text("1. Visit console.groq.com in your browser")
                    Text("2. Create a free account or sign in")
                    Text("3. Go to API Keys section")
                    Text("4. Click \"Create API Key\"")
                    Text("5. Copy the key (starts with gsk_)")
                    Text("6. Paste it in the API Key field here")
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "💡 Groq offers a generous free tier — no credit card required!",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showApiKeyInfo = false }) { Text("Got it!") }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (onClick != null) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsItemWithContent(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        content()
    }
}

@Composable
fun ApiKeyDialog(
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var apiKey by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Key, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Set Groq API Key") },
        text = {
            Column {
                Text(
                    "Enter your Groq API key to enable AI features.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    placeholder = { Text("gsk_...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (showKey) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showKey) "Hide" else "Show"
                            )
                        }
                    }
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "🔒 Your key is encrypted and stored securely on your device.",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (apiKey.isNotBlank()) onSave(apiKey.trim()) },
                enabled = apiKey.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

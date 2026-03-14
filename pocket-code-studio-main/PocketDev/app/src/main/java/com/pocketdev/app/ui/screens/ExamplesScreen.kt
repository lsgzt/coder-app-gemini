package com.pocketdev.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketdev.app.data.models.Language
import com.pocketdev.app.utils.CodeExample
import com.pocketdev.app.utils.CodeExamples

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamplesScreen(onLoadExample: (CodeExample) -> Unit) {
    var selectedLanguage by remember { mutableStateOf<Language?>(null) }
    var previewExample by remember { mutableStateOf<CodeExample?>(null) }

    val filteredExamples = remember(selectedLanguage) {
        if (selectedLanguage == null) CodeExamples.getAllExamples()
        else CodeExamples.getExamplesForLanguage(selectedLanguage!!)
    }

    val filterLanguages = listOf(Language.PYTHON, Language.JAVASCRIPT, Language.HTML)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Code Examples") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Language filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedLanguage == null,
                        onClick = { selectedLanguage = null },
                        label = { Text("All") },
                        leadingIcon = if (selectedLanguage == null) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
                items(filterLanguages) { lang ->
                    FilterChip(
                        selected = selectedLanguage == lang,
                        onClick = {
                            selectedLanguage = if (selectedLanguage == lang) null else lang
                        },
                        label = { Text("${lang.icon} ${lang.displayName}") },
                        leadingIcon = if (selectedLanguage == lang) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Examples list
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredExamples) { example ->
                    ExampleCard(
                        example = example,
                        onPreview = { previewExample = example },
                        onLoad = { onLoadExample(example) }
                    )
                }
            }
        }
    }

    // Preview dialog
    previewExample?.let { example ->
        ExamplePreviewDialog(
            example = example,
            onLoad = {
                onLoadExample(example)
                previewExample = null
            },
            onDismiss = { previewExample = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExampleCard(
    example: CodeExample,
    onPreview: () -> Unit,
    onLoad: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(example.language.icon, fontSize = 20.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = example.title,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text = example.language.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = example.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Code preview snippet
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clickable { onPreview() }
            ) {
                Text(
                    text = example.code.lines().take(4).joinToString("\n"),
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(8.dp),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onPreview) {
                    Icon(Icons.Default.Visibility, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Preview")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onLoad) {
                    Icon(Icons.Default.OpenInNew, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Load")
                }
            }
        }
    }
}

@Composable
fun ExamplePreviewDialog(
    example: CodeExample,
    onLoad: () -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = androidx.compose.foundation.rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(example.language.icon, fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(example.title, style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(modifier = Modifier.heightIn(max = 420.dp)) {
                Text(
                    text = example.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp)
                            .verticalScroll(scrollState)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = example.code,
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onLoad) {
                Icon(Icons.Default.OpenInNew, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Open in Editor")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

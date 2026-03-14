package com.pocketdev.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pocketdev.app.data.models.Project
import com.pocketdev.app.viewmodels.EditorViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    viewModel: EditorViewModel,
    onOpenProject: () -> Unit
) {
    val projects by viewModel.filteredProjects.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Project?>(null) }
    var showNewProjectDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Projects") },
                actions = {
                    IconButton(onClick = { showNewProjectDialog = true }) {
                        Icon(Icons.Default.Add, "New Project")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::setSearchQuery,
                placeholder = { Text("Search projects...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, "Clear search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            if (projects.isEmpty()) {
                EmptyProjectsView(
                    onCreateNew = { showNewProjectDialog = true }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(projects, key = { it.id }) { project ->
                        ProjectCard(
                            project = project,
                            onClick = {
                                viewModel.loadProject(project)
                                onOpenProject()
                            },
                            onDelete = { showDeleteDialog = project },
                            onDuplicate = { viewModel.duplicateProject(project) }
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation
    showDeleteDialog?.let { project ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Project?") },
            text = { Text("Are you sure you want to delete \"${project.name}\"? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProject(project)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }

    // New project dialog
    if (showNewProjectDialog) {
        NewProjectDialog(
            onCreate = { name, language ->
                viewModel.newFile(language)
                viewModel.saveProject(name)
                showNewProjectDialog = false
                onOpenProject()
            },
            onDismiss = { showNewProjectDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectCard(
    project: Project,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Language icon
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = project.language.icon,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text = project.language.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = dateFormat.format(Date(project.modifiedAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "More options")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Open") },
                        leadingIcon = { Icon(Icons.Default.OpenInNew, null) },
                        onClick = { onClick(); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate") },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                        onClick = { onDuplicate(); showMenu = false }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = { onDelete(); showMenu = false }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyProjectsView(onCreateNew: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📁", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(16.dp))
            Text(
                "No projects yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Create a new project to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onCreateNew) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("New Project")
            }
        }
    }
}

@Composable
fun NewProjectDialog(
    onCreate: (String, com.pocketdev.app.data.models.Language) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf(com.pocketdev.app.data.models.Language.PYTHON) }
    var showLanguageMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Project") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text("Language:", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Box {
                    OutlinedButton(
                        onClick = { showLanguageMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${selectedLanguage.icon} ${selectedLanguage.displayName}")
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(
                        expanded = showLanguageMenu,
                        onDismissRequest = { showLanguageMenu = false }
                    ) {
                        com.pocketdev.app.data.models.Language.values().forEach { lang ->
                            DropdownMenuItem(
                                text = { Text("${lang.icon} ${lang.displayName}") },
                                onClick = {
                                    selectedLanguage = lang
                                    showLanguageMenu = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) onCreate(name.trim(), selectedLanguage)
                },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

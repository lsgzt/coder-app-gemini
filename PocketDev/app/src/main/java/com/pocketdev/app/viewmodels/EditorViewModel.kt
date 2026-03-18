package com.pocketdev.app.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pocketdev.app.data.models.*
import com.pocketdev.app.execution.ExecutionManager
import com.pocketdev.app.repository.GroqRepository
import com.pocketdev.app.repository.ProjectRepository
import com.pocketdev.app.utils.NetworkUtils
import com.pocketdev.app.utils.PreferencesManager
import com.pocketdev.app.utils.SecureStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first

import com.pocketdev.app.execution.TerminalManager
import com.pocketdev.app.execution.AIExecutionAgent
import com.pocketdev.app.execution.TerminalMessage
import com.pocketdev.app.execution.TerminalMessageType

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    private val projectRepository = ProjectRepository(context)
    private val groqRepository = GroqRepository()
    private val executionManager = ExecutionManager(context)
    private val secureStorage = SecureStorage(context)
    private val prefsManager = PreferencesManager(context)

    val terminalManager = TerminalManager()
    private val aiExecutionAgent = AIExecutionAgent(
        executionManager = executionManager,
        groqRepository = groqRepository,
        terminalManager = terminalManager,
        updateCode = { newCode -> updateCode(newCode) },
        getApiKey = {
            secureStorage.groqApiKey
        }
    )

    // Current editor state
    private val _currentFiles = MutableStateFlow<List<ProjectFile>>(emptyList())
    val currentFiles: StateFlow<List<ProjectFile>> = _currentFiles.asStateFlow()

    private val _activeFileIndex = MutableStateFlow(0)
    val activeFileIndex: StateFlow<Int> = _activeFileIndex.asStateFlow()

    private val _currentCode = MutableStateFlow("")
    val currentCode: StateFlow<String> = _currentCode.asStateFlow()

    private val _currentLanguage = MutableStateFlow(Language.PYTHON)
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    private val _currentProjectId = MutableStateFlow<Long?>(null)
    val currentProjectId: StateFlow<Long?> = _currentProjectId.asStateFlow()

    private val _currentProjectName = MutableStateFlow("Untitled")
    val currentProjectName: StateFlow<String> = _currentProjectName.asStateFlow()

    // Execution state
    private val _executionState = MutableStateFlow<UiState<ExecutionResult>>(UiState.Idle)
    val executionState: StateFlow<UiState<ExecutionResult>> = _executionState.asStateFlow()

    private val _htmlContent = MutableStateFlow<String?>(null)
    val htmlContent: StateFlow<String?> = _htmlContent.asStateFlow()

    private val _stdInput = MutableStateFlow("")
    val stdInput: StateFlow<String> = _stdInput.asStateFlow()

    fun updateStdInput(input: String) {
        _stdInput.value = input
    }

    // AI state
    private val _aiState = MutableStateFlow<UiState<AiResult>>(UiState.Idle)
    val aiState: StateFlow<UiState<AiResult>> = _aiState

    private val _ghostSuggestion = MutableStateFlow<String?>(null)
    val ghostSuggestion: StateFlow<String?> = _ghostSuggestion

    private val _ghostConfidence = MutableStateFlow<String>("MEDIUM")
    val ghostConfidence: StateFlow<String> = _ghostConfidence

    private val _diffSuggestion = MutableStateFlow<AiResult?>(null)
    val diffSuggestion: StateFlow<AiResult?> = _diffSuggestion

    private var ghostSuggestionJob: Job? = null

    // Save state
    private val _saveState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val saveState: StateFlow<UiState<Unit>> = _saveState.asStateFlow()

    private var lastAiPrompt = ""
    private var lastAiResult = ""

    // Auto-save job
    private var autoSaveJob: Job? = null
    private var hasUnsavedChanges = false

    // All projects
    val allProjects: StateFlow<List<Project>> = projectRepository.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredProjects: StateFlow<List<Project>> = combine(
        allProjects,
        searchQuery
    ) { projects, query ->
        if (query.isBlank()) projects
        else projects.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.language.displayName.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            val lastProjectId = prefsManager.lastProjectId.first()
            val unsavedCode = prefsManager.unsavedCode.first()
            val unsavedLanguage = prefsManager.unsavedLanguage.first()

            if (lastProjectId != -1L) {
                val project = projectRepository.getProjectById(lastProjectId)
                if (project != null) {
                    loadProject(project)
                    if (unsavedCode != null) {
                        _currentCode.value = unsavedCode
                        hasUnsavedChanges = true
                    }
                } else {
                    newFile()
                    if (unsavedCode != null) {
                        _currentCode.value = unsavedCode
                        hasUnsavedChanges = true
                    }
                }
            } else {
                newFile()
                if (unsavedCode != null) {
                    _currentCode.value = unsavedCode
                    hasUnsavedChanges = true
                }
                if (unsavedLanguage != null) {
                    try {
                        _currentLanguage.value = Language.valueOf(unsavedLanguage)
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }
        }
    }

    private var saveJob: kotlinx.coroutines.Job? = null

    fun updateCode(code: String) {
        _currentCode.value = code
        hasUnsavedChanges = true
        
        val files = _currentFiles.value.toMutableList()
        val activeIndex = _activeFileIndex.value
        if (activeIndex in files.indices) {
            files[activeIndex] = files[activeIndex].copy(code = code)
            _currentFiles.value = files
        }

        // Clear ghost suggestion but do not cancel the job, so it can run after typing
        _ghostSuggestion.value = null

        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            prefsManager.setUnsavedCode(code)
        }
    }

    fun switchFile(index: Int) {
        if (index in _currentFiles.value.indices) {
            _activeFileIndex.value = index
            val file = _currentFiles.value[index]
            _currentCode.value = file.code
            _currentLanguage.value = file.language
            rejectGhostSuggestion()
        }
    }

    fun addFile(name: String, language: Language) {
        val newFile = ProjectFile(name = name, language = language, code = getDefaultCode(language))
        val updatedFiles = _currentFiles.value + newFile
        _currentFiles.value = updatedFiles
        switchFile(updatedFiles.size - 1)
        hasUnsavedChanges = true
    }

    fun setLanguage(language: Language) {
        _currentLanguage.value = language
        viewModelScope.launch {
            prefsManager.setUnsavedLanguage(language.name)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadProject(project: Project) {
        _currentFiles.value = project.files
        if (project.files.isNotEmpty()) {
            _activeFileIndex.value = 0
            _currentCode.value = project.files[0].code
            _currentLanguage.value = project.files[0].language
        } else {
            _currentCode.value = project.code
            _currentLanguage.value = project.language
        }
        _currentProjectId.value = project.id
        _currentProjectName.value = project.name
        _executionState.value = UiState.Idle
        _htmlContent.value = null
        hasUnsavedChanges = false
        viewModelScope.launch {
            prefsManager.setLastProjectId(project.id)
            prefsManager.setUnsavedCode(null)
            prefsManager.setUnsavedLanguage(null)
        }
    }

    fun newFile(language: Language = Language.PYTHON) {
        val defaultCode = getDefaultCode(language)
        val defaultFileName = "main${language.extension}"
        val initialFile = ProjectFile(name = defaultFileName, language = language, code = defaultCode)
        _currentFiles.value = listOf(initialFile)
        _activeFileIndex.value = 0
        _currentCode.value = defaultCode
        _currentLanguage.value = language
        _currentProjectId.value = null
        _currentProjectName.value = "Untitled"
        _executionState.value = UiState.Idle
        _htmlContent.value = null
        hasUnsavedChanges = false
        viewModelScope.launch {
            prefsManager.setLastProjectId(-1L)
            prefsManager.setUnsavedCode(null)
            prefsManager.setUnsavedLanguage(null)
        }
    }

    fun runCode() {
        val code = _currentCode.value
        val language = _currentLanguage.value
        val input = _stdInput.value

        if (code.isBlank()) {
            terminalManager.appendError("No code to execute. Write some code first!")
            return
        }

        if (!executionManager.isExecutable(language)) {
            terminalManager.appendError(
                "${language.displayName} execution is not supported.\n" +
                        "Executable languages: Python, JavaScript, HTML"
            )
            return
        }

        viewModelScope.launch {
            terminalManager.clearTerminal()
            terminalManager.appendStatusMessage("Running script...")
            
            val result = executionManager.execute(code, language, input)

            if (result.isSuccess) {
                terminalManager.appendOutput(result.output)
                terminalManager.appendStatusMessage("Execution successful.")
                if (language == Language.HTML) {
                    _htmlContent.value = result.output
                }
            } else {
                terminalManager.appendError(result.error ?: "Unknown error")
            }
            
            _executionState.value = UiState.Success(result)
        }
    }

    fun runWithAiFix() {
        val code = _currentCode.value
        val language = _currentLanguage.value
        val input = _stdInput.value

        if (code.isBlank()) {
            terminalManager.appendError("No code to execute. Write some code first!")
            return
        }

        if (!executionManager.isExecutable(language)) {
            terminalManager.appendError(
                "${language.displayName} execution is not supported.\n" +
                        "Executable languages: Python, JavaScript, HTML"
            )
            return
        }

        viewModelScope.launch {
            terminalManager.clearTerminal()
            _executionState.value = UiState.Loading
            val result = aiExecutionAgent.runWithAiFix(code, language, input)
            if (result != null) {
                if (language == Language.HTML && result.isSuccess) {
                    _htmlContent.value = result.output
                }
                _executionState.value = UiState.Success(result)
            } else {
                _executionState.value = UiState.Idle
            }
        }
    }

    fun clearOutput() {
        _executionState.value = UiState.Idle
        _htmlContent.value = null
    }

    fun saveProject(name: String? = null) {
        viewModelScope.launch {
            _saveState.value = UiState.Loading
            try {
                val projectName = name ?: _currentProjectName.value
                val existingId = _currentProjectId.value

                val project = Project(
                    id = existingId ?: 0,
                    name = projectName,
                    language = _currentLanguage.value,
                    code = _currentCode.value,
                    files = _currentFiles.value,
                    modifiedAt = System.currentTimeMillis()
                )

                val savedId = projectRepository.saveProject(project)
                _currentProjectId.value = savedId
                _currentProjectName.value = projectName
                hasUnsavedChanges = false
                _saveState.value = UiState.Success(Unit)
                prefsManager.setLastProjectId(savedId)
                prefsManager.setUnsavedCode(null)
                prefsManager.setUnsavedLanguage(null)
            } catch (e: Exception) {
                _saveState.value = UiState.Error("Failed to save: ${e.message}")
            }
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            try {
                projectRepository.deleteProject(project)
                if (_currentProjectId.value == project.id) {
                    newFile()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun duplicateProject(project: Project) {
        viewModelScope.launch {
            try {
                projectRepository.duplicateProject(project)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = UiState.Idle
    }

    // AI Features
    fun fixBug() {
        callAiFeature { files, activeFileName, key, model ->
            groqRepository.fixBug(files, activeFileName, key, model)
        }
    }

    fun explainCode() {
        val files = _currentFiles.value
        val activeFileName = if (files.isNotEmpty()) files[_activeFileIndex.value].name else ""
        val code = _currentCode.value
        val language = _currentLanguage.value
        lastAiPrompt = buildString {
            append("Explain this ${language.displayName} code in simple, beginner-friendly terms.\n\n")
            append("```${language.displayName.lowercase()}\n$code\n```\n\n")
            append("Break down what each part does step-by-step. ")
            append("Use simple language suitable for students who are learning to code. ")
            append("Include:\n")
            append("1. What the code does overall\n")
            append("2. A step-by-step breakdown of each part\n")
            append("3. Any important concepts used\n")
            append("4. Tips for beginners")
        }
        callAiFeature { f, a, key, model ->
            val result = groqRepository.explainCode(f, a, key, model)
            lastAiResult = result.content
            result
        }
    }

    fun askFollowUpQuestion(question: String) {
        val apiKey = secureStorage.groqApiKey
        if (apiKey.isBlank()) {
            _aiState.value = UiState.Error("Groq API key not set.")
            return
        }
        if (!NetworkUtils.isOnline(context)) {
            _aiState.value = UiState.Error("No internet connection.")
            return
        }
        viewModelScope.launch {
            _aiState.value = UiState.Loading
            val model = prefsManager.aiModel.first()
            val result = groqRepository.askFollowUp(lastAiPrompt, lastAiResult, question, apiKey, model)
            if (result.isSuccess) {
                lastAiResult = lastAiResult + "\n\n**Q: $question**\n\n" + result.content
                _aiState.value = UiState.Success(result.copy(content = lastAiResult))
            } else {
                _aiState.value = UiState.Error(result.errorMessage ?: "Failed to get follow-up answer")
            }
        }
    }

    fun improveCode() {
        callAiFeature { files, activeFileName, key, model ->
            groqRepository.improveCode(files, activeFileName, key, model)
        }
    }

    fun writeCodeWithAi(prompt: String) {
        val files = _currentFiles.value

        val apiKey = secureStorage.groqApiKey
        if (apiKey.isBlank()) {
            _aiState.value = UiState.Error(
                "Groq API key not set.\nPlease add your API key in Settings to use AI features."
            )
            return
        }

        if (!NetworkUtils.isOnline(context)) {
            _aiState.value = UiState.Error(
                "No internet connection.\nPlease connect to the internet to use AI features."
            )
            return
        }

        viewModelScope.launch {
            _aiState.value = UiState.Loading
            val model = prefsManager.aiModel.first()
            val activeFileName = if (files.isNotEmpty()) files[_activeFileIndex.value].name else "main.py"
            val result = groqRepository.modifyCode(prompt, files, activeFileName, apiKey, model)
            if (result.isSuccess && result.patches.isNotEmpty()) {
                _aiState.value = UiState.Success(result.copy(isEdit = true))
            } else {
                _aiState.value = UiState.Error(result.errorMessage ?: "AI request failed to generate patches")
            }
        }
    }

    fun editCodeWithAi(prompt: String) {
        val files = _currentFiles.value

        val apiKey = secureStorage.groqApiKey
        if (apiKey.isBlank()) {
            _aiState.value = UiState.Error(
                "Groq API key not set.\nPlease add your API key in Settings to use AI features."
            )
            return
        }

        if (!NetworkUtils.isOnline(context)) {
            _aiState.value = UiState.Error(
                "No internet connection.\nPlease connect to the internet to use AI features."
            )
            return
        }

        viewModelScope.launch {
            _aiState.value = UiState.Loading
            val model = prefsManager.aiModel.first()
            val activeFileName = if (files.isNotEmpty()) files[_activeFileIndex.value].name else ""
            val result = groqRepository.editCode(prompt, files, activeFileName, apiKey, model)
            if (result.isSuccess && result.patches.isNotEmpty() && result.isEdit) {
                _aiState.value = UiState.Success(result)
            } else {
                _aiState.value = UiState.Error(result.errorMessage ?: "AI response did not contain valid patches")
            }
        }
    }

    fun getPreviewCode(fileName: String, result: AiResult): String {
        val file = _currentFiles.value.find { it.name == fileName } ?: return ""
        val patches = result.patches.filter { it.fileName == fileName }
        if (patches.isEmpty()) return file.code
        
        val lines = file.code.lines().toMutableList()
        // Apply patches in reverse order to avoid index shifting
        val sortedPatches = patches.sortedByDescending { it.editStart }
        
        for (patch in sortedPatches) {
            val startIndex = maxOf(0, patch.editStart - 1)
            val endIndex = minOf(lines.size, patch.editEnd)
            
            if (startIndex <= endIndex) {
                // Remove old lines
                for (i in startIndex until endIndex) {
                    if (startIndex < lines.size) {
                        lines.removeAt(startIndex)
                    }
                }
                
                // Insert new lines
                val newLines = patch.newCode.lines()
                lines.addAll(startIndex, newLines)
            }
        }
        
        return lines.joinToString("\n")
    }

    fun requestGhostSuggestion(cursorPosition: Int) {
        ghostSuggestionJob?.cancel()
        ghostSuggestionJob = viewModelScope.launch {
            delay(400) // Wait for 400ms of inactivity
            val code = _currentCode.value
            val language = _currentLanguage.value
            val apiKey = secureStorage.groqApiKey
            if (apiKey.isBlank() || !NetworkUtils.isOnline(context)) return@launch

            val model = prefsManager.aiModel.first()
            val result = groqRepository.getGhostSuggestion(code, cursorPosition, language, apiKey, model)
            if (result.isSuccess && result.content.isNotBlank()) {
                if (result.confidence == "LOW") {
                    // Do not auto-show low confidence suggestions
                    _ghostSuggestion.value = null
                    _diffSuggestion.value = null
                } else if (result.isEdit || result.content.lines().size > 2) {
                    // Show smart diff popup for complex edits
                    _ghostSuggestion.value = null
                    _diffSuggestion.value = result
                } else {
                    _ghostSuggestion.value = result.content
                    _ghostConfidence.value = result.confidence
                    _diffSuggestion.value = null
                }
            }
        }
    }

    fun acceptGhostSuggestion(cursorPosition: Int) {
        val suggestion = _ghostSuggestion.value ?: return
        val code = _currentCode.value
        val newCode = code.substring(0, cursorPosition) + suggestion + code.substring(cursorPosition)
        updateCode(newCode)
        _ghostSuggestion.value = null
    }

    fun acceptGhostSuggestionLine(cursorPosition: Int): Int {
        val suggestion = _ghostSuggestion.value ?: return 0
        val code = _currentCode.value
        
        // Find the first newline in the suggestion
        val newlineIndex = suggestion.indexOf('\n')
        val lineToAccept = if (newlineIndex != -1) {
            suggestion.substring(0, newlineIndex + 1)
        } else {
            suggestion
        }
        
        val newCode = code.substring(0, cursorPosition) + lineToAccept + code.substring(cursorPosition)
        updateCode(newCode)
        
        // Update the remaining suggestion
        if (newlineIndex != -1 && newlineIndex + 1 < suggestion.length) {
            _ghostSuggestion.value = suggestion.substring(newlineIndex + 1)
        } else {
            _ghostSuggestion.value = null
        }
        
        return lineToAccept.length
    }

    fun acceptDiffSuggestion(cursorPosition: Int) {
        val suggestion = _diffSuggestion.value ?: return
        val code = _currentCode.value
        
        if (suggestion.isEdit) {
            // For REPLACE type, we need to replace the current line or block
            // A simple implementation: replace the current line
            val lines = code.lines()
            var currentLineIndex = 0
            var charCount = 0
            for (i in lines.indices) {
                charCount += lines[i].length + 1 // +1 for newline
                if (charCount > cursorPosition) {
                    currentLineIndex = i
                    break
                }
            }
            
            val newLines = lines.toMutableList()
            newLines[currentLineIndex] = suggestion.content
            updateCode(newLines.joinToString("\n"))
        } else {
            // For APPEND type but > 2 lines
            val newCode = code.substring(0, cursorPosition) + suggestion.content + code.substring(cursorPosition)
            updateCode(newCode)
        }
        
        _diffSuggestion.value = null
    }

    fun rejectDiffSuggestion() {
        _diffSuggestion.value = null
    }

    fun expandGhostToDiff() {
        val suggestion = _ghostSuggestion.value ?: return
        val confidence = _ghostConfidence.value
        _ghostSuggestion.value = null
        _diffSuggestion.value = AiResult(
            content = suggestion,
            confidence = confidence,
            isEdit = false // It's an APPEND type since it was a ghost suggestion
        )
    }

    fun rejectGhostSuggestion() {
        ghostSuggestionJob?.cancel()
        _ghostSuggestion.value = null
        _diffSuggestion.value = null
    }

    fun clearGhostSuggestion() {
        _ghostSuggestion.value = null
        _diffSuggestion.value = null
    }

    fun applyAiEdit(result: AiResult) {
        val patches = result.patches
        if (patches.isEmpty()) return
        
        val currentFiles = _currentFiles.value.toMutableList()
        
        // Group patches by file
        val patchesByFile = patches.groupBy { it.fileName }
        
        for ((fileName, filePatches) in patchesByFile) {
            val fileIndex = currentFiles.indexOfFirst { it.name == fileName }
            if (fileIndex != -1) {
                val file = currentFiles[fileIndex]
                val lines = file.code.lines().toMutableList()
                
                // Apply patches in reverse order to avoid index shifting
                val sortedPatches = filePatches.sortedByDescending { it.editStart }
                
                for (patch in sortedPatches) {
                    // Convert 1-based line numbers to 0-based indices
                    val startIndex = maxOf(0, patch.editStart - 1)
                    val endIndex = minOf(lines.size, patch.editEnd)
                    
                    if (startIndex <= endIndex) {
                        // Remove old lines
                        for (i in startIndex until endIndex) {
                            if (startIndex < lines.size) {
                                lines.removeAt(startIndex)
                            }
                        }
                        
                        // Insert new lines
                        val newLines = patch.newCode.lines()
                        lines.addAll(startIndex, newLines)
                    }
                }
                
                val newCode = lines.joinToString("\n")
                currentFiles[fileIndex] = file.copy(code = newCode)
            }
        }
        
        _currentFiles.value = currentFiles
        
        // Update current code if active file was modified
        val activeIndex = _activeFileIndex.value
        if (activeIndex in currentFiles.indices) {
            _currentCode.value = currentFiles[activeIndex].code
        }
        
        clearGhostSuggestion()
        
        hasUnsavedChanges = true
        saveProject()
        dismissAiResult()
    }

    private fun callAiFeature(
        action: suspend (List<com.pocketdev.app.data.models.ProjectFile>, String, String, String) -> AiResult
    ) {
        val files = _currentFiles.value
        val activeFileName = if (files.isNotEmpty()) files[_activeFileIndex.value].name else ""

        if (files.isEmpty()) {
            _aiState.value = UiState.Error("No code to analyze. Write some code first!")
            return
        }

        val apiKey = secureStorage.groqApiKey
        if (apiKey.isBlank()) {
            _aiState.value = UiState.Error(
                "Groq API key not set.\nPlease add your API key in Settings to use AI features."
            )
            return
        }

        if (!NetworkUtils.isOnline(context)) {
            _aiState.value = UiState.Error(
                "No internet connection.\nPlease connect to the internet to use AI features."
            )
            return
        }

        viewModelScope.launch {
            _aiState.value = UiState.Loading
            val model = prefsManager.aiModel.first()
            val result = action(files, activeFileName, apiKey, model)
            _aiState.value = if (result.isSuccess) {
                UiState.Success(result)
            } else {
                UiState.Error(result.errorMessage ?: "AI request failed")
            }
        }
    }

    fun chatWithAi(message: String) {
        val files = _currentFiles.value
        val activeFileName = if (files.isNotEmpty()) files[_activeFileIndex.value].name else "main.py"
        
        terminalManager.appendOutput("> $message")
        terminalManager.appendStatusMessage("AI is thinking...")
        
        viewModelScope.launch {
            val apiKey = secureStorage.groqApiKey
            if (apiKey.isBlank()) {
                terminalManager.appendError("Groq API key not set. Please add it in Settings.")
                return@launch
            }
            
            val model = prefsManager.aiModel.first()
            val result = groqRepository.editCode(message, files, activeFileName, apiKey, model)
            if (result.isSuccess) {
                terminalManager.appendAgentMessage(result.content)
            } else {
                terminalManager.appendError(result.errorMessage ?: "Unknown error")
            }
        }
    }

    fun applyAiCode(code: String) {
        _currentCode.value = code
        hasUnsavedChanges = true
        _aiState.value = UiState.Idle
    }

    fun dismissAiResult() {
        _aiState.value = UiState.Idle
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        if (_currentProjectId.value != null) {
            autoSaveJob = viewModelScope.launch {
                delay(30_000) // Auto-save after 30 seconds of inactivity
                if (hasUnsavedChanges) {
                    val projectId = _currentProjectId.value
                    if (projectId != null) {
                        projectRepository.updateCode(projectId, _currentCode.value)
                        hasUnsavedChanges = false
                    }
                }
            }
        }
    }

    private fun getDefaultCode(language: Language): String {
        return when (language) {
            Language.PYTHON -> "# Python\nprint(\"Hello, World!\")\n"
            Language.JAVASCRIPT -> "// JavaScript\nconsole.log(\"Hello, World!\");\n"
            Language.HTML -> "<!DOCTYPE html>\n<html>\n<head><title>My Page</title></head>\n<body>\n    <h1>Hello, World!</h1>\n</body>\n</html>"
            Language.CSS -> "/* CSS */\nbody {\n    font-family: Arial, sans-serif;\n    background: #f5f5f5;\n}\n"
            Language.JAVA -> "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}\n"
            Language.CPP -> "#include <iostream>\nusing namespace std;\n\nint main() {\n    cout << \"Hello, World!\" << endl;\n    return 0;\n}\n"
            Language.KOTLIN -> "fun main() {\n    println(\"Hello, World!\")\n}\n"
            Language.JSON -> "{\n    \"message\": \"Hello, World!\"\n}\n"
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()
    }
}

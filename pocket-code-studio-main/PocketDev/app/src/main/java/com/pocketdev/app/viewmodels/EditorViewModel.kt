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

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    private val projectRepository = ProjectRepository(context)
    private val groqRepository = GroqRepository()
    private val executionManager = ExecutionManager(context)
    private val secureStorage = SecureStorage(context)
    private val prefsManager = PreferencesManager(context)

    // Current editor state
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

    // AI state
    private val _aiState = MutableStateFlow<UiState<AiResult>>(UiState.Idle)
    val aiState: StateFlow<UiState<AiResult>> = _aiState.asStateFlow()

    // Save state
    private val _saveState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val saveState: StateFlow<UiState<Unit>> = _saveState.asStateFlow()

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

    fun updateCode(code: String) {
        _currentCode.value = code
        hasUnsavedChanges = true
        scheduleAutoSave()
    }

    fun setLanguage(language: Language) {
        _currentLanguage.value = language
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadProject(project: Project) {
        _currentCode.value = project.code
        _currentLanguage.value = project.language
        _currentProjectId.value = project.id
        _currentProjectName.value = project.name
        _executionState.value = UiState.Idle
        _htmlContent.value = null
        hasUnsavedChanges = false
    }

    fun newFile(language: Language = Language.PYTHON) {
        _currentCode.value = getDefaultCode(language)
        _currentLanguage.value = language
        _currentProjectId.value = null
        _currentProjectName.value = "Untitled"
        _executionState.value = UiState.Idle
        _htmlContent.value = null
        hasUnsavedChanges = false
    }

    fun runCode() {
        val code = _currentCode.value
        val language = _currentLanguage.value

        if (code.isBlank()) {
            _executionState.value = UiState.Error("No code to execute. Write some code first!")
            return
        }

        if (!executionManager.isExecutable(language)) {
            _executionState.value = UiState.Error(
                "${language.displayName} execution is not supported.\n" +
                        "Executable languages: Python, JavaScript, HTML"
            )
            return
        }

        viewModelScope.launch {
            _executionState.value = UiState.Loading

            val result = executionManager.execute(code, language)

            if (language == Language.HTML && result.isSuccess) {
                _htmlContent.value = result.output
            }

            _executionState.value = UiState.Success(result)
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
                    modifiedAt = System.currentTimeMillis()
                )

                val savedId = projectRepository.saveProject(project)
                _currentProjectId.value = savedId
                _currentProjectName.value = projectName
                hasUnsavedChanges = false
                _saveState.value = UiState.Success(Unit)
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
        callAiFeature { code, language, key, model ->
            groqRepository.fixBug(code, language, key, model)
        }
    }

    fun explainCode() {
        callAiFeature { code, language, key, model ->
            groqRepository.explainCode(code, language, key, model)
        }
    }

    fun improveCode() {
        callAiFeature { code, language, key, model ->
            groqRepository.improveCode(code, language, key, model)
        }
    }

    private fun callAiFeature(
        action: suspend (String, Language, String, String) -> AiResult
    ) {
        val code = _currentCode.value
        val language = _currentLanguage.value

        if (code.isBlank()) {
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
            val result = action(code, language, apiKey, model)
            _aiState.value = if (result.isSuccess) {
                UiState.Success(result)
            } else {
                UiState.Error(result.errorMessage ?: "AI request failed")
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

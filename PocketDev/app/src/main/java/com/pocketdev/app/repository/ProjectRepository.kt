package com.pocketdev.app.repository

import android.content.Context
import com.pocketdev.app.data.db.AppDatabase
import com.pocketdev.app.data.models.Language
import com.pocketdev.app.data.models.Project
import com.pocketdev.app.data.models.ProjectFile
import kotlinx.coroutines.flow.Flow

class ProjectRepository(context: Context) {

    private val projectDao = AppDatabase.getDatabase(context).projectDao()

    fun getAllProjects(): Flow<List<Project>> = projectDao.getAllProjects()

    fun searchProjects(query: String): Flow<List<Project>> = projectDao.searchProjects(query)

    fun getProjectsByLanguage(language: Language): Flow<List<Project>> =
        projectDao.getProjectsByLanguage(language)

    suspend fun getProjectById(id: Long): Project? = projectDao.getProjectById(id)

    suspend fun createProject(
        name: String,
        language: Language,
        code: String = getDefaultCode(language)
    ): Long {
        val defaultFileName = "main${language.extension}"
        val initialFile = ProjectFile(
            name = defaultFileName,
            language = language,
            code = code
        )
        val project = Project(
            name = name,
            language = language,
            code = code, // Keep for backward compatibility
            files = listOf(initialFile),
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis()
        )
        return projectDao.insertProject(project)
    }

    suspend fun saveProject(project: Project): Long {
        return if (project.id == 0L) {
            projectDao.insertProject(project)
        } else {
            projectDao.updateProject(project.copy(modifiedAt = System.currentTimeMillis()))
            project.id
        }
    }

    suspend fun updateCode(projectId: Long, code: String) {
        // This is deprecated, we should update files instead.
        // But for backward compatibility, we'll update the first file if it exists.
        val project = projectDao.getProjectById(projectId)
        if (project != null) {
            val updatedFiles = project.files.toMutableList()
            if (updatedFiles.isNotEmpty()) {
                updatedFiles[0] = updatedFiles[0].copy(code = code)
            } else {
                updatedFiles.add(ProjectFile(name = "main${project.language.extension}", language = project.language, code = code))
            }
            projectDao.updateProject(project.copy(code = code, files = updatedFiles, modifiedAt = System.currentTimeMillis()))
        }
    }

    suspend fun deleteProject(project: Project) {
        projectDao.deleteProject(project)
    }

    suspend fun duplicateProject(project: Project): Long {
        val copy = project.copy(
            id = 0,
            name = "${project.name} (copy)",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis()
        )
        return projectDao.insertProject(copy)
    }

    private fun getDefaultCode(language: Language): String {
        return when (language) {
            Language.PYTHON -> "# Python - Hello World\nprint(\"Hello, World!\")\n\n# Your code here\n"
            Language.JAVASCRIPT -> "// JavaScript - Hello World\nconsole.log(\"Hello, World!\");\n\n// Your code here\n"
            Language.HTML -> """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Page</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 50px auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        h1 { color: #333; }
    </style>
</head>
<body>
    <h1>Hello, World!</h1>
    <p>Welcome to my HTML page.</p>

    <script>
        console.log("Page loaded!");
    </script>
</body>
</html>"""
            Language.CSS -> "/* CSS Styles */\nbody {\n    font-family: Arial, sans-serif;\n    background-color: #f5f5f5;\n    margin: 0;\n    padding: 20px;\n}\n\nh1 {\n    color: #333;\n    text-align: center;\n}\n"
            Language.JAVA -> "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}\n"
            Language.CPP -> "#include <iostream>\nusing namespace std;\n\nint main() {\n    cout << \"Hello, World!\" << endl;\n    return 0;\n}\n"
            Language.KOTLIN -> "fun main() {\n    println(\"Hello, World!\")\n}\n"
            Language.JSON -> "{\n    \"name\": \"My Project\",\n    \"version\": \"1.0.0\",\n    \"description\": \"A sample JSON file\"\n}\n"
        }
    }
}

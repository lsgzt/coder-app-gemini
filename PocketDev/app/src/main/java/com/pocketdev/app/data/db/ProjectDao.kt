package com.pocketdev.app.data.db

import androidx.room.*
import com.pocketdev.app.data.models.Language
import com.pocketdev.app.data.models.Project
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Query("SELECT * FROM projects ORDER BY modifiedAt DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Long): Project?

    @Query("SELECT * FROM projects WHERE name LIKE '%' || :query || '%' ORDER BY modifiedAt DESC")
    fun searchProjects(query: String): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE language = :language ORDER BY modifiedAt DESC")
    fun getProjectsByLanguage(language: Language): Flow<List<Project>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun getProjectCount(): Int

    @Query("UPDATE projects SET code = :code, modifiedAt = :modifiedAt WHERE id = :id")
    suspend fun updateProjectCode(id: Long, code: String, modifiedAt: Long = System.currentTimeMillis())
}

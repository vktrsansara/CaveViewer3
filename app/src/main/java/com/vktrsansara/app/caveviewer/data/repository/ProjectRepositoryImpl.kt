package com.vktrsansara.app.caveviewer.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vktrsansara.app.caveviewer.data.database.ProjectDatabase
import com.vktrsansara.app.caveviewer.domain.model.CadastralItem
import com.vktrsansara.app.caveviewer.domain.model.EntranceCoordinate
import com.vktrsansara.app.caveviewer.domain.model.MapLocation
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
import com.vktrsansara.app.caveviewer.domain.model.ProjectInfo
import com.vktrsansara.app.caveviewer.domain.repository.ProjectRepository
import com.vktrsansara.app.caveviewer.domain.tile.TileCutProgress
import com.vktrsansara.app.caveviewer.domain.tile.TileCutter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private val Context.projectDataStore: DataStore<Preferences> by preferencesDataStore(name = "caveviewer_projects")

class ProjectRepositoryImpl(
    private val context: Context
) : ProjectRepository {

    private object PreferencesKeys {
        val ACTIVE_PROJECT_NAME = stringPreferencesKey("active_project_name")
    }

    override val activeProjectNameFlow: Flow<String?> = context.projectDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.ACTIVE_PROJECT_NAME]
        }

    override suspend fun setActiveProjectName(name: String?) {
        context.projectDataStore.edit { preferences ->
            if (name != null && name.isNotBlank()) {
                preferences[PreferencesKeys.ACTIVE_PROJECT_NAME] = name
            } else {
                preferences.remove(PreferencesKeys.ACTIVE_PROJECT_NAME)
            }
        }
    }

    private fun getProjectsBaseDir(): File {
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val baseDir = File(documentsDir, "CaveViewer/Projects")
        if (!baseDir.exists()) {
            val created = baseDir.mkdirs()
            if (!created && !baseDir.exists()) {
                val fallbackDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "CaveViewer/Projects")
                fallbackDir.mkdirs()
                return fallbackDir
            }
        }
        return baseDir
    }

    override suspend fun getProjectsList(): List<ProjectInfo> = withContext(Dispatchers.IO) {
        val baseDir = getProjectsBaseDir()
        val dirs = baseDir.listFiles { file -> file.isDirectory && !file.name.startsWith(".") } ?: return@withContext emptyList()

        dirs.map { dir ->
            val sqliteFile = File(dir, "thismap.sqlite")
            val mapFile = File(dir, "map/image.png")
            val size = calculateDirectorySize(dir)
            ProjectInfo(
                name = dir.name,
                path = dir.absolutePath,
                lastModified = dir.lastModified(),
                sizeBytes = size,
                hasMap = sqliteFile.exists() || mapFile.exists()
            )
        }.sortedByDescending { it.lastModified }
    }

    override suspend fun getProjectDir(projectName: String): File? = withContext(Dispatchers.IO) {
        val baseDir = getProjectsBaseDir()
        val dir = File(baseDir, projectName)
        if (dir.exists() && dir.isDirectory) dir else null
    }

    override suspend fun getProjectMetadata(projectName: String): MapMetadata? = withContext(Dispatchers.IO) {
        val dir = getProjectDir(projectName) ?: return@withContext null
        val dbFile = File(dir, "thismap.sqlite")
        if (!dbFile.exists()) return@withContext null
        ProjectDatabase(dbFile).getMetadata()
    }

    override suspend fun updateProjectMetadata(
        originalProjectName: String,
        metadata: MapMetadata
    ): Result<MapMetadata> = withContext(Dispatchers.IO) {
        try {
            val baseDir = getProjectsBaseDir()
            val oldDir = File(baseDir, originalProjectName)
            if (!oldDir.exists()) {
                return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            }

            val cleanNewName = metadata.projectName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_")
            if (cleanNewName.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Название проекта не может быть пустым"))
            }

            val targetDir = if (cleanNewName != originalProjectName) {
                val newDir = File(baseDir, cleanNewName)
                if (newDir.exists()) {
                    return@withContext Result.failure(IllegalStateException("Проект с названием «$cleanNewName» уже существует"))
                }
                val renamed = oldDir.renameTo(newDir)
                if (!renamed) {
                    return@withContext Result.failure(IllegalStateException("Не удалось переименовать папку проекта"))
                }
                newDir
            } else {
                oldDir
            }

            val dbFile = File(targetDir, "thismap.sqlite")
            val db = ProjectDatabase(dbFile)
            val updated = metadata.copy(projectName = cleanNewName)
            db.saveMetadata(updated)

            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProjectLocation(projectName: String): MapLocation = withContext(Dispatchers.IO) {
        val dir = getProjectDir(projectName) ?: return@withContext MapLocation()
        val dbFile = File(dir, "thismap.sqlite")
        if (!dbFile.exists()) return@withContext MapLocation()
        ProjectDatabase(dbFile).getLocation()
    }

    override suspend fun saveProjectLocation(projectName: String, location: MapLocation): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            ProjectDatabase(dbFile).saveLocation(location)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProjectEntrances(projectName: String): List<EntranceCoordinate> = withContext(Dispatchers.IO) {
        val dir = getProjectDir(projectName) ?: return@withContext emptyList()
        val dbFile = File(dir, "thismap.sqlite")
        if (!dbFile.exists()) return@withContext emptyList()
        ProjectDatabase(dbFile).getEntrances()
    }

    override suspend fun saveProjectEntrances(projectName: String, entrances: List<EntranceCoordinate>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            ProjectDatabase(dbFile).saveEntrances(entrances)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProjectCadastralData(projectName: String): Map<String, List<CadastralItem>> = withContext(Dispatchers.IO) {
        val dir = getProjectDir(projectName) ?: return@withContext emptyMap()
        val dbFile = File(dir, "thismap.sqlite")
        if (!dbFile.exists()) return@withContext emptyMap()
        ProjectDatabase(dbFile).getCadastralData()
    }

    override suspend fun saveProjectCadastralData(projectName: String, data: Map<String, List<CadastralItem>>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            ProjectDatabase(dbFile).saveCadastralData(data)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProject(projectName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val baseDir = getProjectsBaseDir()
            val dir = File(baseDir, projectName)
            if (dir.exists()) {
                val trashDir = File(baseDir, ".trash")
                if (!trashDir.exists()) {
                    trashDir.mkdirs()
                }
                val targetTrash = File(trashDir, "${projectName}_${System.currentTimeMillis()}")
                val renamed = dir.renameTo(targetTrash)
                val dirToDelete = if (renamed) targetTrash else dir

                // Background async purge: UI receives instant sub-millisecond response
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val process = Runtime.getRuntime().exec(arrayOf("rm", "-rf", dirToDelete.absolutePath))
                        process.waitFor()
                    } catch (_: Exception) {}
                    if (dirToDelete.exists()) {
                        dirToDelete.deleteRecursively()
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createRasterProject(
        projectName: String,
        imageUri: Uri,
        onProgress: (TileCutProgress) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        var createdProjectDir: File? = null
        try {
            val cleanName = projectName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_")
            if (cleanName.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Название проекта не может быть пустым"))
            }

            val baseDir = getProjectsBaseDir()
            val projectDir = File(baseDir, cleanName)
            if (projectDir.exists()) {
                return@withContext Result.failure(IllegalStateException("Проект с названием «$cleanName» уже существует"))
            }

            val mapDir = File(projectDir, "map")
            if (!mapDir.mkdirs() && !mapDir.exists()) {
                return@withContext Result.failure(IllegalStateException("Не удалось создать папку проекта: ${mapDir.absolutePath}"))
            }
            createdProjectDir = projectDir

            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return@withContext Result.failure(IllegalStateException("Не удалось прочитать выбранный файл карты"))

            val bitmap = inputStream.use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: return@withContext Result.failure(IllegalStateException("Не удалось распознать формат изображения"))

            // Save source map image
            val targetFile = File(mapDir, "image.png")
            FileOutputStream(targetFile).use { out ->
                val compressed = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                if (!compressed) {
                    return@withContext Result.failure(IllegalStateException("Ошибка при сохранении PNG карты"))
                }
            }

            // Cut tile pyramid and save thismap.sqlite
            TileCutter.cutTiles(
                projectName = cleanName,
                projectDir = projectDir,
                sourceBitmap = bitmap,
                onProgress = onProgress
            )

            bitmap.recycle()
            Result.success(projectDir)
        } catch (e: CancellationException) {
            // User cancelled tile generation -> immediately purge partial directory
            createdProjectDir?.let { dir ->
                try {
                    dir.deleteRecursively()
                } catch (_: Exception) {}
            }
            throw e
        } catch (e: Exception) {
            createdProjectDir?.let { dir ->
                try {
                    dir.deleteRecursively()
                } catch (_: Exception) {}
            }
            Result.failure(e)
        }
    }

    private val projectSizeCache = java.util.concurrent.ConcurrentHashMap<String, Pair<Long, Long>>()

    private fun calculateDirectorySize(directory: File): Long {
        val lastMod = directory.lastModified()
        val cached = projectSizeCache[directory.absolutePath]
        if (cached != null && cached.first == lastMod) {
            return cached.second
        }

        var length: Long = 0
        val files = directory.listFiles() ?: return 0
        for (file in files) {
            length += if (file.isFile) {
                file.length()
            } else {
                calculateDirectorySize(file)
            }
        }
        projectSizeCache[directory.absolutePath] = Pair(lastMod, length)
        return length
    }
}

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
import com.vktrsansara.app.caveviewer.domain.model.LayerLine
import com.vktrsansara.app.caveviewer.domain.model.LayerPoint
import com.vktrsansara.app.caveviewer.domain.model.LineLayer
import com.vktrsansara.app.caveviewer.domain.model.MapLocation
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
import com.vktrsansara.app.caveviewer.domain.model.PointLayer
import com.vktrsansara.app.caveviewer.domain.model.ProjectInfo
import com.vktrsansara.app.caveviewer.domain.repository.ProjectRepository
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.vktrsansara.app.caveviewer.domain.tile.TileCutProgress
import com.vktrsansara.app.caveviewer.domain.tile.TileCutter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

    override suspend fun saveScaleBinding(
        projectName: String,
        pixelsPerMeter: Double,
        scaleMeters: Double
    ): Result<MapMetadata> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            val db = ProjectDatabase(dbFile)
            val updated = db.updateScaleBinding(pixelsPerMeter, scaleMeters)
                ?: return@withContext Result.failure(IllegalStateException("Не удалось обновить масштаб в базе данных"))
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveNorthBinding(
        projectName: String,
        angleNorth: Double
    ): Result<MapMetadata> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            val db = ProjectDatabase(dbFile)
            val updated = db.updateNorthBinding(angleNorth)
                ?: return@withContext Result.failure(IllegalStateException("Не удалось обновить направление севера в базе данных"))
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

    override suspend fun addProjectEntrance(
        projectName: String,
        entrance: EntranceCoordinate
    ): Result<List<EntranceCoordinate>> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            val db = ProjectDatabase(dbFile)
            val current = db.getEntrances().toMutableList()
            current.add(entrance.copy(pointIndex = current.size))
            db.saveEntrances(current)
            Result.success(current)
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
                // Release SQLite connection pools and cache handles before moving/deleting
                try {
                    SQLiteDatabase.releaseMemory()
                } catch (_: Exception) {}

                val trashDir = File(baseDir, ".trash")
                if (!trashDir.exists()) {
                    trashDir.mkdirs()
                }
                val targetTrash = File(trashDir, "${projectName}_${System.currentTimeMillis()}")
                val renamed = dir.renameTo(targetTrash)
                val dirToDelete = if (renamed) targetTrash else dir

                // Background async purge using managed SupervisorJob scope:
                // Fast atomic rename gives instant UI feedback, while file deletion is handled reliably
                repositoryScope.launch {
                    try {
                        dirToDelete.deleteRecursively()
                    } catch (e: Exception) {
                        Log.e("ProjectRepository", "Error purging deleted project directory: ${dirToDelete.absolutePath}", e)
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

    override suspend fun importProject(
        archiveUri: Uri,
        onProgress: (progress: Float, statusText: String) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        val tempDir = File(context.cacheDir, "temp_import_${System.currentTimeMillis()}").apply { mkdirs() }
        val tempZipFile = File(tempDir, "archive.zip")
        try {
            onProgress(0.02f, "Чтение файла архива...")

            // 1. Copy stream to temp archive file with progress
            val totalFileSize = getFileSizeFromUri(archiveUri)
            context.contentResolver.openInputStream(archiveUri)?.use { input ->
                FileOutputStream(tempZipFile).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var bytesCopied: Long = 0
                    var read: Int
                    var lastUpdate = System.currentTimeMillis()

                    while (input.read(buffer).also { read = it } >= 0) {
                        output.write(buffer, 0, read)
                        bytesCopied += read
                        val now = System.currentTimeMillis()
                        if (now - lastUpdate > 100) {
                            lastUpdate = now
                            if (totalFileSize > 0) {
                                val frac = (bytesCopied.toFloat() / totalFileSize).coerceIn(0f, 1f)
                                onProgress(0.02f + frac * 0.13f, "Чтение архива (${(frac * 100).toInt()}%)...")
                            } else {
                                onProgress(0.08f, "Чтение архива (${bytesCopied / (1024 * 1024)} МБ)...")
                            }
                        }
                    }
                }
            } ?: return@withContext Result.failure(Exception("Не удалось открыть выбранный файл архива"))

            // Check for CaveViewer V1 legacy project structure (metadata.json without thismap.sqlite)
            val zipEntries = java.util.zip.ZipFile(tempZipFile).use { it.entries().asSequence().map { e -> e.name }.toList() }
            if (com.vktrsansara.app.caveviewer.data.importer.LegacyCaveViewerImporter.isLegacyProject(zipEntries)) {
                return@withContext com.vktrsansara.app.caveviewer.data.importer.LegacyCaveViewerImporter.importLegacyZip(
                    context = context,
                    zipFile = tempZipFile,
                    targetProjectsBaseDir = getProjectsBaseDir(),
                    onProgress = onProgress
                )
            }

            onProgress(0.15f, "Подготовка к распаковке...")

            // 2. Unpack archive to temp extracted directory with progress
            val extractDir = File(tempDir, "extracted").apply { mkdirs() }
            unzipWithProgress(tempZipFile, extractDir) { entryIndex, totalEntries, _ ->
                val frac = if (totalEntries > 0) entryIndex.toFloat() / totalEntries else 0f
                val currentProgress = 0.15f + frac * 0.60f
                onProgress(currentProgress, "Распаковка: $entryIndex из $totalEntries файлов...")
            }

            onProgress(0.75f, "Проверка структуры проекта...")

            // 3. Locate thismap.sqlite (search recursively for project root)
            val sqliteFiles = extractDir.walkTopDown().filter { it.isFile && it.name.equals("thismap.sqlite", ignoreCase = true) }.toList()
            if (sqliteFiles.isEmpty()) {
                return@withContext Result.failure(Exception("Некорректный архив: проект должен содержать thismap.sqlite и папку tiles"))
            }

            val sqliteFile = sqliteFiles.first()
            val projectRoot = sqliteFile.parentFile ?: extractDir

            // 4. Validate tiles folder
            val tilesDir = File(projectRoot, "tiles")
            if (!tilesDir.exists() || !tilesDir.isDirectory) {
                return@withContext Result.failure(Exception("Некорректный архив: проект должен содержать thismap.sqlite и папку tiles"))
            }

            // 5. Read project name from metadata or archive filename
            val db = ProjectDatabase(sqliteFile)
            val metadata = try { db.getMetadata() } catch (e: Exception) { null }
            val rawName = metadata?.projectName?.takeIf { it.isNotBlank() }
                ?: getFileNameFromUri(archiveUri)?.substringBeforeLast(".")
                ?: "Imported_Project"

            val sanitizedName = rawName
                .replace(Regex("[\\\\/:*?\"<>|]"), "_")
                .trim()
                .ifBlank { "Imported_Project" }

            // 6. Create unique target directory in Documents/CaveViewer/Projects
            val baseDir = getProjectsBaseDir()
            var finalName = sanitizedName
            var finalDir = File(baseDir, finalName)
            var counter = 1
            while (finalDir.exists()) {
                finalName = "$sanitizedName ($counter)"
                finalDir = File(baseDir, finalName)
                counter++
            }
            finalDir.mkdirs()

            // 7. Copy project files into final directory with progress
            onProgress(0.80f, "Сохранение проекта в хранилище...")

            projectRoot.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    file.copyRecursively(File(finalDir, file.name), overwrite = true)
                } else {
                    file.copyTo(File(finalDir, file.name), overwrite = true)
                }
            }

            onProgress(0.95f, "Настройка метаданных и тайлов...")

            // Ensure tiles/.v3_aligned marker exists
            val finalTilesDir = File(finalDir, "tiles")
            if (finalTilesDir.exists()) {
                val alignedMarker = File(finalTilesDir, ".v3_aligned")
                if (!alignedMarker.exists()) {
                    try {
                        alignedMarker.createNewFile()
                    } catch (_: Exception) {}
                }
            }

            // Sync metadata project name if finalName differs
            val finalDbFile = File(finalDir, "thismap.sqlite")
            if (finalDbFile.exists()) {
                try {
                    val finalDb = ProjectDatabase(finalDbFile)
                    val currentMeta = finalDb.getMetadata()
                    if (currentMeta != null && currentMeta.projectName != finalName) {
                        finalDb.saveMetadata(currentMeta.copy(projectName = finalName))
                    }
                } catch (_: Exception) {}
            }

            onProgress(1.0f, "Импорт успешно завершен!")
            Result.success(finalName)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Ошибка распаковки архива", e))
        } finally {
            try {
                tempDir.deleteRecursively()
            } catch (_: Exception) {}
        }
    }

    private fun unzipWithProgress(
        zipFile: File,
        targetDir: File,
        onEntryProgress: (entryIndex: Int, totalEntries: Int, entryName: String) -> Unit
    ) {
        java.util.zip.ZipFile(zipFile).use { zip ->
            val total = zip.size()
            var current = 0
            var lastProgressTime = 0L

            val entries = zip.entries()
            val canonicalDestPath = targetDir.canonicalPath
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                current++
                val entryFile = File(targetDir, entry.name)
                val canonicalEntryPath = entryFile.canonicalPath

                // Zip Slip protection
                if (!canonicalEntryPath.startsWith(canonicalDestPath + File.separator) && canonicalEntryPath != canonicalDestPath) {
                    throw SecurityException("Небезопасный путь в архиве: ${entry.name}")
                }

                if (entry.isDirectory) {
                    entryFile.mkdirs()
                } else {
                    entryFile.parentFile?.mkdirs()
                    zip.getInputStream(entry).use { input ->
                        FileOutputStream(entryFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                val now = System.currentTimeMillis()
                if (now - lastProgressTime > 80 || current == total) {
                    lastProgressTime = now
                    onEntryProgress(current, total, entry.name)
                }
            }
        }
    }

    private fun getFileSizeFromUri(uri: Uri): Long {
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                        if (sizeIndex != -1) {
                            return cursor.getLong(sizeIndex)
                        }
                    }
                }
            } catch (_: Exception) {}
        }
        return -1L
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            return cursor.getString(nameIndex)
                        }
                    }
                }
            } catch (_: Exception) {}
        }
        return uri.lastPathSegment?.substringAfterLast('/')
    }

    // ==========================================
    // Point Layers & Layer Points CRUD
    // ==========================================

    override suspend fun getPointLayers(projectName: String): List<PointLayer> = withContext(Dispatchers.IO) {
        val dir = getProjectDir(projectName) ?: return@withContext emptyList()
        val dbFile = File(dir, "thismap.sqlite")
        if (!dbFile.exists()) return@withContext emptyList()
        ProjectDatabase(dbFile).getPointLayers()
    }

    override suspend fun insertPointLayer(projectName: String, layer: PointLayer): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            val id = ProjectDatabase(dbFile).insertPointLayer(layer)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePointLayer(projectName: String, layer: PointLayer): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            ProjectDatabase(dbFile).updatePointLayer(layer)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePointLayer(projectName: String, layerId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            ProjectDatabase(dbFile).deletePointLayer(layerId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleLayerVisibility(projectName: String, layerId: Long, isVisible: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            ProjectDatabase(dbFile).toggleLayerVisibility(layerId, isVisible)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPointsForLayer(projectName: String, layerId: Long): List<LayerPoint> = withContext(Dispatchers.IO) {
        val dir = getProjectDir(projectName) ?: return@withContext emptyList()
        val dbFile = File(dir, "thismap.sqlite")
        if (!dbFile.exists()) return@withContext emptyList()
        ProjectDatabase(dbFile).getPointsForLayer(layerId)
    }

    override suspend fun getAllVisiblePoints(projectName: String): List<LayerPoint> = withContext(Dispatchers.IO) {
        val dir = getProjectDir(projectName) ?: return@withContext emptyList()
        val dbFile = File(dir, "thismap.sqlite")
        if (!dbFile.exists()) return@withContext emptyList()
        ProjectDatabase(dbFile).getAllVisiblePoints()
    }

    override suspend fun insertLayerPoint(projectName: String, point: LayerPoint): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            val id = ProjectDatabase(dbFile).insertLayerPoint(point)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLayerPoint(projectName: String, point: LayerPoint): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            ProjectDatabase(dbFile).updateLayerPoint(point)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteLayerPoint(projectName: String, pointId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            ProjectDatabase(dbFile).deleteLayerPoint(pointId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Line Layers ---

    override suspend fun getLineLayers(projectName: String): List<LineLayer> = withContext(Dispatchers.IO) {
        val dir = getProjectDir(projectName) ?: return@withContext emptyList()
        val dbFile = File(dir, "thismap.sqlite")
        if (!dbFile.exists()) return@withContext emptyList()
        ProjectDatabase(dbFile).getLineLayers()
    }

    override suspend fun insertLineLayer(projectName: String, layer: LineLayer): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            val id = ProjectDatabase(dbFile).insertLineLayer(layer)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLineLayer(projectName: String, layer: LineLayer): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            ProjectDatabase(dbFile).updateLineLayer(layer)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteLineLayer(projectName: String, layerId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            ProjectDatabase(dbFile).deleteLineLayer(layerId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleLineLayerVisibility(projectName: String, layerId: Long, isVisible: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            ProjectDatabase(dbFile).toggleLineLayerVisibility(layerId, isVisible)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Layer Lines ---

    override suspend fun getLinesForLayer(projectName: String, layerId: Long): List<LayerLine> = withContext(Dispatchers.IO) {
        val dir = getProjectDir(projectName) ?: return@withContext emptyList()
        val dbFile = File(dir, "thismap.sqlite")
        if (!dbFile.exists()) return@withContext emptyList()
        ProjectDatabase(dbFile).getLinesForLayer(layerId)
    }

    override suspend fun getAllVisibleLines(projectName: String): List<LayerLine> = withContext(Dispatchers.IO) {
        val dir = getProjectDir(projectName) ?: return@withContext emptyList()
        val dbFile = File(dir, "thismap.sqlite")
        if (!dbFile.exists()) return@withContext emptyList()
        ProjectDatabase(dbFile).getAllVisibleLines()
    }

    override suspend fun insertLayerLine(projectName: String, line: LayerLine): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            val id = ProjectDatabase(dbFile).insertLayerLine(line)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLayerLine(projectName: String, line: LayerLine): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            ProjectDatabase(dbFile).updateLayerLine(line)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteLayerLine(projectName: String, lineId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            ProjectDatabase(dbFile).deleteLayerLine(lineId)
            Result.success(Unit)
        } catch (e: Exception) {
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

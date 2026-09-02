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
import com.vktrsansara.app.caveviewer.domain.repository.PasswordRequiredException
import com.vktrsansara.app.caveviewer.domain.repository.InvalidPasswordException
import com.vktrsansara.app.caveviewer.engine.maplibre.CaveMapBounds
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import net.lingala.zip4j.model.enums.AesKeyStrength
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

    private val databaseCache = java.util.concurrent.ConcurrentHashMap<String, ProjectDatabase>()

    private fun getDatabase(dbFile: File): ProjectDatabase {
        val path = try { dbFile.canonicalPath } catch (_: Exception) { dbFile.absolutePath }
        return databaseCache.computeIfAbsent(path) {
            ProjectDatabase(dbFile)
        }
    }

    private fun closeDatabase(dbFile: File) {
        val path = try { dbFile.canonicalPath } catch (_: Exception) { dbFile.absolutePath }
        val db = databaseCache.remove(path)
        db?.close()
        ProjectDatabase.invalidateSchemaCache(dbFile)
    }

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
        getDatabase(dbFile).getMetadata()
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
                closeDatabase(File(oldDir, "thismap.sqlite"))
                val renamed = oldDir.renameTo(newDir)
                if (!renamed) {
                    return@withContext Result.failure(IllegalStateException("Не удалось переименовать папку проекта"))
                }
                newDir
            } else {
                oldDir
            }

            val dbFile = File(targetDir, "thismap.sqlite")
            val db = getDatabase(dbFile)
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
            val db = getDatabase(dbFile)
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
            val db = getDatabase(dbFile)
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
        getDatabase(dbFile).getLocation()
    }

    override suspend fun saveProjectLocation(projectName: String, location: MapLocation): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            getDatabase(dbFile).saveLocation(location)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProjectEntrances(projectName: String): List<EntranceCoordinate> = withContext(Dispatchers.IO) {
        val dir = getProjectDir(projectName) ?: return@withContext emptyList()
        val dbFile = File(dir, "thismap.sqlite")
        if (!dbFile.exists()) return@withContext emptyList()
        getDatabase(dbFile).getEntrances()
    }

    override suspend fun saveProjectEntrances(projectName: String, entrances: List<EntranceCoordinate>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            getDatabase(dbFile).saveEntrances(entrances)
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
            val db = getDatabase(dbFile)
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
        getDatabase(dbFile).getCadastralData()
    }

    override suspend fun saveProjectCadastralData(projectName: String, data: Map<String, List<CadastralItem>>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            getDatabase(dbFile).saveCadastralData(data)
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
                val dbFile = File(dir, "thismap.sqlite")
                closeDatabase(dbFile)

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

            // 1. Copy source stream directly to map/image.png without loading entire bitmap into RAM
            val targetFile = File(mapDir, "image.png")
            val copied = context.contentResolver.openInputStream(imageUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output) > 0
                }
            } ?: false

            if (!copied || !targetFile.exists() || targetFile.length() == 0L) {
                return@withContext Result.failure(IllegalStateException("Не удалось прочитать выбранный файл карты"))
            }

            // 2. Check image dimensions using inJustDecodeBounds to prevent OOM
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(targetFile.absolutePath, boundsOptions)
            val rawWidth = boundsOptions.outWidth
            val rawHeight = boundsOptions.outHeight
            if (rawWidth <= 0 || rawHeight <= 0) {
                return@withContext Result.failure(IllegalStateException("Не удалось распознать формат изображения карты"))
            }

            // 3. Compute safe inSampleSize based on available JVM heap and texture limits
            val maxMemory = Runtime.getRuntime().maxMemory()
            val usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            val availableMemory = maxMemory - usedMemory
            val maxAllowedBytes = (availableMemory * 0.45).toLong().coerceAtMost(256L * 1024 * 1024)

            var sampleSize = 1
            var w = rawWidth
            var h = rawHeight
            val maxDim = 8192

            while (w * h * 4L > maxAllowedBytes || w > maxDim || h > maxDim) {
                sampleSize *= 2
                w = rawWidth / sampleSize
                h = rawHeight / sampleSize
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val bitmap = BitmapFactory.decodeFile(targetFile.absolutePath, decodeOptions)
                ?: return@withContext Result.failure(IllegalStateException("Не удалось декодировать изображение карты"))

            try {
                // Cut tile pyramid and save thismap.sqlite
                TileCutter.cutTiles(
                    projectName = cleanName,
                    projectDir = projectDir,
                    sourceBitmap = bitmap,
                    onProgress = onProgress
                )
            } finally {
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
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
        password: String?,
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

            // 2. Check if archive is valid and whether it is password-protected using Zip4j
            val zip4j = net.lingala.zip4j.ZipFile(tempZipFile)
            if (!zip4j.isValidZipFile) {
                return@withContext Result.failure(Exception("Выбранный файл не является корректным архивом проекта (.cvproj или .zip)"))
            }

            val isEncrypted = try { zip4j.isEncrypted } catch (_: Exception) { false }
            if (isEncrypted) {
                if (password.isNullOrEmpty()) {
                    return@withContext Result.failure(PasswordRequiredException())
                }
                zip4j.setPassword(password.toCharArray())
            }

            // Check for CaveViewer V1 legacy project structure (metadata.json without thismap.sqlite)
            if (!isEncrypted) {
                try {
                    val zipEntries = java.util.zip.ZipFile(tempZipFile).use { it.entries().asSequence().map { e -> e.name }.toList() }
                    if (com.vktrsansara.app.caveviewer.data.importer.LegacyCaveViewerImporter.isLegacyProject(zipEntries)) {
                        return@withContext com.vktrsansara.app.caveviewer.data.importer.LegacyCaveViewerImporter.importLegacyZip(
                            context = context,
                            zipFile = tempZipFile,
                            targetProjectsBaseDir = getProjectsBaseDir(),
                            onProgress = onProgress
                        )
                    }
                } catch (_: Exception) {}
            }

            onProgress(0.15f, "Подготовка к распаковке...")

            // 3. Unpack archive to temp extracted directory with progress
            val extractDir = File(tempDir, "extracted").apply { mkdirs() }
            if (isEncrypted) {
                try {
                    val headers = zip4j.fileHeaders
                    val totalHeaders = headers.size
                    var extractedCount = 0
                    var lastProgressTime = 0L

                    for (header in headers) {
                        zip4j.extractFile(header, extractDir.absolutePath)
                        extractedCount++
                        val now = System.currentTimeMillis()
                        if (now - lastProgressTime > 80 || extractedCount == totalHeaders) {
                            lastProgressTime = now
                            val frac = extractedCount.toFloat() / totalHeaders
                            onProgress(0.15f + frac * 0.60f, "Распаковка: $extractedCount из $totalHeaders файлов...")
                        }
                    }
                } catch (e: net.lingala.zip4j.exception.ZipException) {
                    val msg = e.message ?: ""
                    if (msg.contains("Wrong Password", ignoreCase = true) ||
                        msg.contains("password", ignoreCase = true) ||
                        e.type == net.lingala.zip4j.exception.ZipException.Type.WRONG_PASSWORD) {
                        return@withContext Result.failure(InvalidPasswordException())
                    }
                    throw e
                }
            } else {
                unzipWithProgress(tempZipFile, extractDir) { entryIndex, totalEntries, _ ->
                    val frac = if (totalEntries > 0) entryIndex.toFloat() / totalEntries else 0f
                    val currentProgress = 0.15f + frac * 0.60f
                    onProgress(currentProgress, "Распаковка: $entryIndex из $totalEntries файлов...")
                }
            }

            onProgress(0.75f, "Проверка структуры проекта...")

            // 4. Locate thismap.sqlite (search recursively for project root)
            val sqliteFiles = extractDir.walkTopDown().filter { it.isFile && it.name.equals("thismap.sqlite", ignoreCase = true) }.toList()
            if (sqliteFiles.isEmpty()) {
                return@withContext Result.failure(Exception("Некорректный проект: архив должен содержать thismap.sqlite и папку tiles"))
            }

            val sqliteFile = sqliteFiles.first()
            val projectRoot = sqliteFile.parentFile ?: extractDir

            // 5. Validate tiles folder
            val tilesDir = File(projectRoot, "tiles")
            if (!tilesDir.exists() || !tilesDir.isDirectory) {
                return@withContext Result.failure(Exception("Некорректный проект: архив должен содержать thismap.sqlite и папку tiles"))
            }

            // 6. Read project name from metadata or archive filename
            val db = getDatabase(sqliteFile)
            val metadata = try { db.getMetadata() } catch (e: Exception) { null }
            val rawName = metadata?.projectName?.takeIf { it.isNotBlank() }
                ?: getFileNameFromUri(archiveUri)?.substringBeforeLast(".")
                ?: "Imported_Project"

            val sanitizedName = rawName
                .replace(Regex("[\\\\/:*?\"<>|]"), "_")
                .trim()
                .ifBlank { "Imported_Project" }

            // 7. Create unique target directory in Documents/CaveViewer/Projects
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

            // 8. Copy project files into final directory with progress
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
                    val finalDb = getDatabase(finalDbFile)
                    val currentMeta = finalDb.getMetadata()
                    if (currentMeta != null && currentMeta.projectName != finalName) {
                        finalDb.saveMetadata(currentMeta.copy(projectName = finalName))
                    }
                } catch (_: Exception) {}
            }

            onProgress(1.0f, "Импорт успешно завершен!")
            Result.success(finalName)
        } catch (e: PasswordRequiredException) {
            Result.failure(e)
        } catch (e: InvalidPasswordException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Ошибка распаковки архива", e))
        } finally {
            try {
                tempDir.deleteRecursively()
            } catch (_: Exception) {}
        }
    }

    override suspend fun exportProject(
        projectName: String,
        outputUri: Uri,
        compressionLevel: Int,
        password: String?,
        onProgress: (progress: Float, statusText: String) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val projectDir = getProjectDir(projectName)
            ?: return@withContext Result.failure(IllegalStateException("Папка проекта «$projectName» не найдена"))

        val tempExportFile = File(context.cacheDir, "export_${System.currentTimeMillis()}.cvproj")
        try {
            onProgress(0.05f, "Проверка структуры проекта...")

            // 1. Проверяем и при необходимости автоматически склеиваем map/image.png из тайлов максимального зума
            val mapDir = File(projectDir, "map").apply { mkdirs() }
            val mapImageFile = File(mapDir, "image.png")
            if (!mapImageFile.exists() || mapImageFile.length() == 0L) {
                onProgress(0.08f, "Сборка исходного растра карты из тайлов...")
                val stitched = reconstructImageFromTiles(projectDir)
                if (!stitched) {
                    Log.w("ProjectRepository", "Не удалось собрать map/image.png из тайлов")
                }
            }

            // 2. Убеждаемся в наличии маркера .v3_aligned в папке тайлов
            val tilesDir = File(projectDir, "tiles")
            if (tilesDir.exists()) {
                val alignedMarker = File(tilesDir, ".v3_aligned")
                if (!alignedMarker.exists()) {
                    try { alignedMarker.createNewFile() } catch (_: Exception) {}
                }
            }

            // 3. Формируем список файлов для упаковки в корень архива
            val filesToPack = mutableListOf<Pair<File, String>>()
            val sqliteFile = File(projectDir, "thismap.sqlite")
            if (sqliteFile.exists()) {
                filesToPack.add(sqliteFile to "thismap.sqlite")
            } else {
                return@withContext Result.failure(IllegalStateException("В проекте отсутствует база данных thismap.sqlite"))
            }

            if (mapImageFile.exists() && mapImageFile.length() > 0L) {
                filesToPack.add(mapImageFile to "map/image.png")
            }

            if (tilesDir.exists()) {
                tilesDir.walkTopDown().filter { it.isFile }.forEach { file ->
                    val relPath = "tiles/" + file.relativeTo(tilesDir).path.replace('\\', '/')
                    filesToPack.add(file to relPath)
                }
            }

            if (filesToPack.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("В проекте нет файлов для экспорта"))
            }

            onProgress(0.15f, "Подготовка архива проекта...")

            // 4. Настраиваем параметры Zip4j (сжатие и опциональное шифрование AES-256)
            val zipMethod = if (compressionLevel == 0) CompressionMethod.STORE else CompressionMethod.DEFLATE
            val zipCompLevel = when (compressionLevel) {
                0 -> CompressionLevel.NO_COMPRESSION
                1 -> CompressionLevel.FASTEST
                9 -> CompressionLevel.MAXIMUM
                else -> CompressionLevel.NORMAL
            }
            val hasPassword = !password.isNullOrBlank()

            val baseParameters = ZipParameters().apply {
                this.compressionMethod = zipMethod
                this.compressionLevel = zipCompLevel
                if (hasPassword) {
                    this.isEncryptFiles = true
                    this.encryptionMethod = EncryptionMethod.AES
                    this.aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256
                }
            }

            // 5. Упаковываем файлы в tempExportFile через ZipOutputStream
            val totalFiles = filesToPack.size
            var packedFiles = 0
            var lastProgressTime = 0L

            FileOutputStream(tempExportFile).use { fos ->
                val zos = if (hasPassword) {
                    net.lingala.zip4j.io.outputstream.ZipOutputStream(fos, password!!.toCharArray())
                } else {
                    net.lingala.zip4j.io.outputstream.ZipOutputStream(fos)
                }

                zos.use { zipOut ->
                    val buffer = ByteArray(64 * 1024)
                    for ((file, entryName) in filesToPack) {
                        val entryParams = ZipParameters(baseParameters).apply {
                            fileNameInZip = entryName
                        }
                        zipOut.putNextEntry(entryParams)

                        file.inputStream().use { input ->
                            var read: Int
                            while (input.read(buffer).also { read = it } >= 0) {
                                zipOut.write(buffer, 0, read)
                            }
                        }
                        zipOut.closeEntry()

                        packedFiles++
                        val now = System.currentTimeMillis()
                        if (now - lastProgressTime > 80 || packedFiles == totalFiles) {
                            lastProgressTime = now
                            val frac = packedFiles.toFloat() / totalFiles
                            val percent = (frac * 100).toInt()
                            onProgress(0.15f + frac * 0.70f, "Сжатие: $packedFiles из $totalFiles файлов ($percent%)...")
                        }
                    }
                }
            }

            onProgress(0.90f, "Сохранение файла проекта...")

            // 6. Копируем готовый архив в outputUri через ContentResolver
            context.contentResolver.openOutputStream(outputUri)?.use { outStream ->
                tempExportFile.inputStream().use { inStream ->
                    inStream.copyTo(outStream)
                }
            } ?: return@withContext Result.failure(IllegalStateException("Не удалось сохранить файл по выбранному пути"))

            onProgress(1.0f, "Экспорт успешно завершен!")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try {
                if (tempExportFile.exists()) tempExportFile.delete()
            } catch (_: Exception) {}
        }
    }

    private fun reconstructImageFromTiles(projectDir: File): Boolean {
        return try {
            val dbFile = File(projectDir, "thismap.sqlite")
            if (!dbFile.exists()) return false
            val db = getDatabase(dbFile)
            val meta = db.getMetadata() ?: return false
            val width = meta.imageWidth
            val height = meta.imageHeight
            val zoomMax = meta.zoomMax
            if (width <= 0 || height <= 0 || zoomMax <= 0) return false

            val tilesDir = File(projectDir, "tiles")
            val zoomDir = File(tilesDir, zoomMax.toString())
            if (!zoomDir.exists()) return false

            val (range, imageTopLeft) = CaveMapBounds.calculateTileRange(width, height, zoomMax, zoomMax)
            val (imageLeftPx, imageTopPx) = imageTopLeft
            val tileSize = CaveMapBounds.TILE_SIZE

            val fullBitmap = try {
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            } catch (_: OutOfMemoryError) {
                Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            }
            val canvas = android.graphics.Canvas(fullBitmap)
            val paint = android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG)

            for (tileX in range.minTileX..range.maxTileX) {
                for (tileY in range.minTileY..range.maxTileY) {
                    val tileFile = File(zoomDir, "$tileX/$tileY.png")
                    if (tileFile.exists()) {
                        val tileBmp = BitmapFactory.decodeFile(tileFile.absolutePath)
                        if (tileBmp != null) {
                            val tileWorldLeft = tileX * tileSize
                            val tileWorldTop = tileY * tileSize
                            val destX = (tileWorldLeft - imageLeftPx).toInt()
                            val destY = (tileWorldTop - imageTopPx).toInt()
                            canvas.drawBitmap(tileBmp, destX.toFloat(), destY.toFloat(), paint)
                            tileBmp.recycle()
                        }
                    }
                }
            }

            val mapDir = File(projectDir, "map").apply { mkdirs() }
            val mapImageFile = File(mapDir, "image.png")
            FileOutputStream(mapImageFile).use { out ->
                fullBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            fullBitmap.recycle()
            mapImageFile.exists() && mapImageFile.length() > 0L
        } catch (e: Exception) {
            Log.e("ProjectRepository", "Error reconstructing map/image.png from tiles", e)
            false
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
        getDatabase(dbFile).getPointLayers()
    }

    override suspend fun insertPointLayer(projectName: String, layer: PointLayer): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            val id = getDatabase(dbFile).insertPointLayer(layer)
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
            getDatabase(dbFile).updatePointLayer(layer)
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
            getDatabase(dbFile).deletePointLayer(layerId)
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
            getDatabase(dbFile).toggleLayerVisibility(layerId, isVisible)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPointsForLayer(projectName: String, layerId: Long): List<LayerPoint> = withContext(Dispatchers.IO) {
        val dir = getProjectDir(projectName) ?: return@withContext emptyList()
        val dbFile = File(dir, "thismap.sqlite")
        if (!dbFile.exists()) return@withContext emptyList()
        getDatabase(dbFile).getPointsForLayer(layerId)
    }

    override suspend fun getAllVisiblePoints(projectName: String): List<LayerPoint> = withContext(Dispatchers.IO) {
        val dir = getProjectDir(projectName) ?: return@withContext emptyList()
        val dbFile = File(dir, "thismap.sqlite")
        if (!dbFile.exists()) return@withContext emptyList()
        getDatabase(dbFile).getAllVisiblePoints()
    }

    override suspend fun insertLayerPoint(projectName: String, point: LayerPoint): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            val id = getDatabase(dbFile).insertLayerPoint(point)
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
            getDatabase(dbFile).updateLayerPoint(point)
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
            getDatabase(dbFile).deleteLayerPoint(pointId)
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
        getDatabase(dbFile).getLineLayers()
    }

    override suspend fun insertLineLayer(projectName: String, layer: LineLayer): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            val id = getDatabase(dbFile).insertLineLayer(layer)
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
            getDatabase(dbFile).updateLineLayer(layer)
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
            getDatabase(dbFile).deleteLineLayer(layerId)
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
            getDatabase(dbFile).toggleLineLayerVisibility(layerId, isVisible)
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
        getDatabase(dbFile).getLinesForLayer(layerId)
    }

    override suspend fun getAllVisibleLines(projectName: String): List<LayerLine> = withContext(Dispatchers.IO) {
        val dir = getProjectDir(projectName) ?: return@withContext emptyList()
        val dbFile = File(dir, "thismap.sqlite")
        if (!dbFile.exists()) return@withContext emptyList()
        getDatabase(dbFile).getAllVisibleLines()
    }

    override suspend fun insertLayerLine(projectName: String, line: LayerLine): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val dir = getProjectDir(projectName)
                ?: return@withContext Result.failure(IllegalStateException("Папка проекта не найдена"))
            val dbFile = File(dir, "thismap.sqlite")
            val id = getDatabase(dbFile).insertLayerLine(line)
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
            getDatabase(dbFile).updateLayerLine(line)
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
            getDatabase(dbFile).deleteLayerLine(lineId)
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

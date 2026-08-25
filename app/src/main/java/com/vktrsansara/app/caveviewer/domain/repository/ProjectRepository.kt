package com.vktrsansara.app.caveviewer.domain.repository

import android.net.Uri
import com.vktrsansara.app.caveviewer.domain.model.EntranceCoordinate
import com.vktrsansara.app.caveviewer.domain.model.MapLocation
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
import com.vktrsansara.app.caveviewer.domain.model.ProjectInfo
import com.vktrsansara.app.caveviewer.domain.tile.TileCutProgress
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ProjectRepository {
    val activeProjectNameFlow: Flow<String?>
    suspend fun setActiveProjectName(name: String?)
    suspend fun createRasterProject(
        projectName: String,
        imageUri: Uri,
        onProgress: (TileCutProgress) -> Unit = {}
    ): Result<File>
    suspend fun getProjectsList(): List<ProjectInfo>
    suspend fun getProjectDir(projectName: String): File?
    suspend fun deleteProject(projectName: String): Result<Unit>
    suspend fun getProjectMetadata(projectName: String): MapMetadata?
    suspend fun updateProjectMetadata(originalProjectName: String, metadata: MapMetadata): Result<MapMetadata>
    suspend fun getProjectLocation(projectName: String): MapLocation
    suspend fun saveProjectLocation(projectName: String, location: MapLocation): Result<Unit>
    suspend fun getProjectEntrances(projectName: String): List<EntranceCoordinate>
    suspend fun saveProjectEntrances(projectName: String, entrances: List<EntranceCoordinate>): Result<Unit>
}

package com.vktrsansara.app.caveviewer.domain.repository

import android.net.Uri
import com.vktrsansara.app.caveviewer.domain.model.CadastralItem
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
    suspend fun saveScaleBinding(projectName: String, pixelsPerMeter: Double, scaleMeters: Double): Result<MapMetadata>
    suspend fun saveNorthBinding(projectName: String, angleNorth: Double): Result<MapMetadata>
    suspend fun getProjectLocation(projectName: String): MapLocation
    suspend fun saveProjectLocation(projectName: String, location: MapLocation): Result<Unit>
    suspend fun getProjectEntrances(projectName: String): List<EntranceCoordinate>
    suspend fun saveProjectEntrances(projectName: String, entrances: List<EntranceCoordinate>): Result<Unit>
    suspend fun addProjectEntrance(projectName: String, entrance: EntranceCoordinate): Result<List<EntranceCoordinate>>
    suspend fun getProjectCadastralData(projectName: String): Map<String, List<CadastralItem>>
    suspend fun saveProjectCadastralData(projectName: String, data: Map<String, List<CadastralItem>>): Result<Unit>
}

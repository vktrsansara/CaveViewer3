package com.vktrsansara.app.caveviewer.domain.repository

import android.net.Uri
import com.vktrsansara.app.caveviewer.domain.model.CadastralItem
import com.vktrsansara.app.caveviewer.domain.model.EntranceCoordinate
import com.vktrsansara.app.caveviewer.domain.model.LayerLine
import com.vktrsansara.app.caveviewer.domain.model.LayerPoint
import com.vktrsansara.app.caveviewer.domain.model.LineLayer
import com.vktrsansara.app.caveviewer.domain.model.MapLocation
import com.vktrsansara.app.caveviewer.domain.model.MapMetadata
import com.vktrsansara.app.caveviewer.domain.model.PointLayer
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

    // Point Layers
    suspend fun getPointLayers(projectName: String): List<PointLayer>
    suspend fun insertPointLayer(projectName: String, layer: PointLayer): Result<Long>
    suspend fun updatePointLayer(projectName: String, layer: PointLayer): Result<Unit>
    suspend fun deletePointLayer(projectName: String, layerId: Long): Result<Unit>
    suspend fun toggleLayerVisibility(projectName: String, layerId: Long, isVisible: Boolean): Result<Unit>

    // Layer Points
    suspend fun getPointsForLayer(projectName: String, layerId: Long): List<LayerPoint>
    suspend fun getAllVisiblePoints(projectName: String): List<LayerPoint>
    suspend fun insertLayerPoint(projectName: String, point: LayerPoint): Result<Long>
    suspend fun updateLayerPoint(projectName: String, point: LayerPoint): Result<Unit>
    suspend fun deleteLayerPoint(projectName: String, pointId: Long): Result<Unit>

    // Line Layers
    suspend fun getLineLayers(projectName: String): List<LineLayer>
    suspend fun insertLineLayer(projectName: String, layer: LineLayer): Result<Long>
    suspend fun updateLineLayer(projectName: String, layer: LineLayer): Result<Unit>
    suspend fun deleteLineLayer(projectName: String, layerId: Long): Result<Unit>
    suspend fun toggleLineLayerVisibility(projectName: String, layerId: Long, isVisible: Boolean): Result<Unit>

    // Layer Lines
    suspend fun getLinesForLayer(projectName: String, layerId: Long): List<LayerLine>
    suspend fun getAllVisibleLines(projectName: String): List<LayerLine>
    suspend fun insertLayerLine(projectName: String, line: LayerLine): Result<Long>
    suspend fun updateLayerLine(projectName: String, line: LayerLine): Result<Unit>
    suspend fun deleteLayerLine(projectName: String, lineId: Long): Result<Unit>
}

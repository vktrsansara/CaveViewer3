package com.vktrsansara.app.caveviewer.domain.model

data class ProjectInfo(
    val name: String,
    val path: String,
    val lastModified: Long,
    val sizeBytes: Long,
    val hasMap: Boolean
)

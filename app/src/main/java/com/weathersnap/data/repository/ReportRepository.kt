package com.weathersnap.data.repository

import com.weathersnap.data.local.dao.DraftDao
import com.weathersnap.data.local.dao.ReportDao
import com.weathersnap.data.local.entity.DraftReportEntity
import com.weathersnap.domain.model.WeatherReport
import com.weathersnap.domain.model.WeatherSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val reportDao: ReportDao,
    private val draftDao: DraftDao
) {
    fun getAllReports(): Flow<List<WeatherReport>> =
        reportDao.getAllReports().map { list -> list.map { it.toReport() } }

    suspend fun saveReport(
        weather: WeatherSnapshot,
        notes: String,
        imagePath: String?,
        originalSize: Long,
        compressedSize: Long
    ): Long = withContext(Dispatchers.IO) {
        val entity = weather.toReportEntity(notes, imagePath, originalSize, compressedSize, System.currentTimeMillis())
        reportDao.insertReport(entity)
    }

    suspend fun saveDraft(
        weather: WeatherSnapshot,
        notes: String,
        imagePath: String?,
        originalSize: Long,
        compressedSize: Long
    ) = withContext(Dispatchers.IO) {
        draftDao.saveDraft(weather.toDraft(notes, imagePath, originalSize, compressedSize))
    }

    suspend fun getDraft(): DraftReportEntity? = withContext(Dispatchers.IO) {
        draftDao.getDraft()
    }

    suspend fun clearDraft() = withContext(Dispatchers.IO) {
        draftDao.clearDraft()
    }

    suspend fun deleteAllReports() = withContext(Dispatchers.IO) {
        reportDao.deleteAllReports()
    }
}

package com.weathersnap.data.local.dao

import androidx.room.*
import com.weathersnap.data.local.entity.ReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteReport(id: Long)
}

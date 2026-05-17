package com.weathersnap.data.local.dao

import androidx.room.*
import com.weathersnap.data.local.entity.DraftReportEntity

@Dao
interface DraftDao {
    @Query("SELECT * FROM draft_report WHERE id = 1")
    suspend fun getDraft(): DraftReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: DraftReportEntity)

    @Query("DELETE FROM draft_report WHERE id = 1")
    suspend fun clearDraft()
}

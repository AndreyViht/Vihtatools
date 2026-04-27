package com.vihttools.mobile.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReport(report: Report): Long

    @Update
    suspend fun updateReport(report: Report)

    @Delete
    suspend fun deleteReport(report: Report)

    @Query("SELECT * FROM reports ORDER BY timestamp DESC LIMIT 10")
    fun getAllReports(): Flow<List<Report>>

    @Query("SELECT * FROM reports WHERE id = :id")
    suspend fun getReportById(id: Int): Report?

    @Query("SELECT * FROM reports WHERE nickname = :nickname AND playerId = :playerId LIMIT 1")
    suspend fun getReportByNicknameAndId(nickname: String, playerId: Int): Report?

    @Query("SELECT COUNT(*) FROM reports WHERE isAnswered = 0")
    fun getUnreadReportCount(): Flow<Int>

    @Query("DELETE FROM reports WHERE timestamp < :cutoffTime")
    suspend fun deleteOldReports(cutoffTime: Long)

    @Query("UPDATE reports SET isAnswered = 1, answeredAt = :timestamp WHERE id = :id")
    suspend fun markAsAnswered(id: Int, timestamp: Long)
}

package com.vihttools.mobile.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTemplate(template: Template)

    @Update
    suspend fun updateTemplate(template: Template)

    @Delete
    suspend fun deleteTemplate(template: Template)

    @Query("SELECT * FROM templates WHERE isActive = 1 ORDER BY `order` ASC")
    fun getAllActiveTemplates(): Flow<List<Template>>

    @Query("SELECT * FROM templates ORDER BY `order` ASC")
    fun getAllTemplates(): Flow<List<Template>>

    @Query("SELECT * FROM templates WHERE id = :id")
    suspend fun getTemplateById(id: Int): Template?

    @Query("UPDATE templates SET `order` = :newOrder WHERE id = :id")
    suspend fun updateTemplateOrder(id: Int, newOrder: Int)

    @Query("DELETE FROM templates")
    suspend fun deleteAllTemplates()

    @Query("SELECT COUNT(*) FROM templates")
    fun getTemplateCount(): Flow<Int>
}

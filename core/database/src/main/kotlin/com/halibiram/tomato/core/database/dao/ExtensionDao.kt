package com.halibiram.tomato.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.halibiram.tomato.core.database.entity.ExtensionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExtensionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExtension(extension: ExtensionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExtensions(extensions: List<ExtensionEntity>) // For batch inserts

    @Query("SELECT * FROM extensions ORDER BY name ASC")
    fun getAllExtensions(): Flow<List<ExtensionEntity>>

    @Query("SELECT * FROM extensions WHERE isEnabled = 1 ORDER BY name ASC")
    fun getEnabledExtensions(): Flow<List<ExtensionEntity>>

    @Query("SELECT * FROM extensions WHERE id = :id")
    suspend fun getExtensionById(id: String): ExtensionEntity?

    @Query("SELECT * FROM extensions WHERE id = :id")
    fun getExtensionByIdFlow(id: String): Flow<ExtensionEntity?>


    @Query("DELETE FROM extensions WHERE id = :id")
    suspend fun deleteExtensionById(id: String) // Renamed for clarity

    @Query("UPDATE extensions SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setEnabled(id: String, isEnabled: Boolean)

    @Query("UPDATE extensions SET loadingError = :error WHERE id = :id")
    suspend fun updateLoadingError(id: String, error: String?)


    @Query("DELETE FROM extensions")
    suspend fun clearAllExtensions()
}

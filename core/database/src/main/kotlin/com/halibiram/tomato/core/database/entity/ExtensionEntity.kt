package com.halibiram.tomato.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "extensions")
data class ExtensionEntity(
    @PrimaryKey val id: String, // Package name or unique ID from the extension manifest
    val name: String,
    val version: String,
    val author: String,
    val description: String?,
    val sourceUri: String?, // URI from where it was installed (e.g., file path, URL for later update checks)
    val iconPath: String?, // Local path to a cached icon (if downloaded or extracted)
    var isEnabled: Boolean,
    val installedAt: Long, // Timestamp of installation
    val apiVersion: Int,
    val className: String,
    val packageName: String? = null,
    val sourceDescription: String? = null,
    val loadingError: String? = null // To store any error that occurred during loading this extension
)

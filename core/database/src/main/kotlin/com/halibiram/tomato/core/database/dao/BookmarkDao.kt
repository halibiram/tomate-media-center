package com.halibiram.tomato.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete // Added for specific delete method
import com.halibiram.tomato.core.database.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    // Query by composite key (mediaId and mediaType)
    @Query("SELECT * FROM bookmarks WHERE mediaId = :mediaId AND mediaType = :mediaType")
    suspend fun getBookmarkByIdAndType(mediaId: String, mediaType: String): BookmarkEntity?

    @Query("SELECT * FROM bookmarks ORDER BY addedDate DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>> // Renamed from getBookmarks

    @Query("SELECT * FROM bookmarks WHERE mediaType = :mediaType ORDER BY addedDate DESC")
    fun getBookmarksByType(mediaType: String): Flow<List<BookmarkEntity>>

    @Query("DELETE FROM bookmarks WHERE mediaId = :mediaId AND mediaType = :mediaType")
    suspend fun deleteBookmark(mediaId: String, mediaType: String) // Changed to use composite key

    // Keep this if you want to delete all bookmarks for a given mediaId regardless of type
    // @Query("DELETE FROM bookmarks WHERE mediaId = :mediaId")
    // suspend fun deleteBookmarksByMediaId(mediaId: String)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity) // Convenience method

    @Query("DELETE FROM bookmarks")
    suspend fun clearBookmarks() // Renamed from deleteAllBookmarks

    // Check if a specific media item (by ID and Type) is bookmarked. Returns Flow.
    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE mediaId = :mediaId AND mediaType = :mediaType LIMIT 1)")
    fun isBookmarkedFlow(mediaId: String, mediaType: String): Flow<Boolean>

    // Suspend function to check if bookmarked (one-shot)
    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE mediaId = :mediaId AND mediaType = :mediaType LIMIT 1)")
    suspend fun isBookmarked(mediaId: String, mediaType: String): Boolean
}

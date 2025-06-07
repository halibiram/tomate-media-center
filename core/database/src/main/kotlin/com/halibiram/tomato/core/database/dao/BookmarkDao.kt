package com.halibiram.tomato.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.halibiram.tomato.core.database.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

// BookmarkDao
@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE mediaId = :mediaId")
    suspend fun removeBookmark(mediaId: String)

    @Query("SELECT * FROM bookmarks WHERE mediaId = :mediaId")
    fun getBookmark(mediaId: String): Flow<BookmarkEntity?>

    @Query("SELECT * FROM bookmarks ORDER BY bookmarkedAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE mediaId = :mediaId)")
    fun isBookmarked(mediaId: String): Flow<Boolean>
}

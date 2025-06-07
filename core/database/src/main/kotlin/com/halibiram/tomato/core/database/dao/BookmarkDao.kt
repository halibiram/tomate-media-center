package com.halibiram.tomato.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.halibiram.tomato.core.database.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("SELECT * FROM bookmarks WHERE mediaId = :mediaId")
    fun getBookmarkById(mediaId: String): Flow<BookmarkEntity?>

    @Query("SELECT * FROM bookmarks ORDER BY bookmarkedDate DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE mediaType = :mediaType ORDER BY bookmarkedDate DESC")
    fun getBookmarksByType(mediaType: String): Flow<List<BookmarkEntity>>

    @Query("DELETE FROM bookmarks WHERE mediaId = :mediaId")
    suspend fun deleteBookmarkById(mediaId: String)

    @Query("DELETE FROM bookmarks")
    suspend fun deleteAllBookmarks()

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE mediaId = :mediaId LIMIT 1)")
    fun isBookmarked(mediaId: String): Flow<Boolean>
}

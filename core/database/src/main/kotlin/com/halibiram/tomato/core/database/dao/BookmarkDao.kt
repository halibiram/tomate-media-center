package com.halibiram.tomato.core.database.dao

import androidx.room.*
import com.halibiram.tomato.core.database.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("SELECT * FROM bookmarks ORDER BY bookmarkedAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE id = :mediaId")
    fun getBookmarkById(mediaId: String): Flow<BookmarkEntity?>

    @Query("DELETE FROM bookmarks WHERE id = :mediaId")
    suspend fun deleteBookmark(mediaId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE id = :mediaId LIMIT 1)")
    fun isBookmarked(mediaId: String): Flow<Boolean>
}

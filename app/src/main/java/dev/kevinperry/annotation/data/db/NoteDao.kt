package dev.kevinperry.annotation.data.db

import androidx.room.*
import dev.kevinperry.annotation.data.model.Note
import dev.kevinperry.annotation.data.SortOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    fun getNotes(searchQuery: String, sortOrder: SortOrder): Flow<List<Note>> {
        return when (sortOrder) {
            SortOrder.BY_TITLE -> getNotesSortedByTitle(searchQuery)
            SortOrder.BY_NEWEST -> getNotesSortedByNewest(searchQuery)
            SortOrder.BY_OLDEST -> getNotesSortedByOldest(searchQuery)
        }
    }

    @Query("SELECT * FROM note_table WHERE title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%' ORDER BY title")
    fun getNotesSortedByTitle(searchQuery: String): Flow<List<Note>>

    @Query("SELECT * FROM note_table WHERE title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%' ORDER BY dateCreated DESC")
    fun getNotesSortedByNewest(searchQuery: String): Flow<List<Note>>

    @Query("SELECT * FROM note_table WHERE title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%' ORDER BY dateCreated ASC")
    fun getNotesSortedByOldest(searchQuery: String): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

}
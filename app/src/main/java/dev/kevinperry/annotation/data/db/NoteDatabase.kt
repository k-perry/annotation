package dev.kevinperry.annotation.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.kevinperry.annotation.data.model.Note

@Database(entities = [Note::class], version = 1)
abstract class NoteDatabase : RoomDatabase() {

    abstract val noteDao: NoteDao

}
package dev.kevinperry.annotation.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.kevinperry.annotation.data.db.NoteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val DATABASE_NAME = "note_database"

    @Singleton
    @Provides
    fun provideDatabase(
        app: Application,
        callback: NoteDatabase.Callback
    ) = Room.databaseBuilder(app, NoteDatabase::class.java, DATABASE_NAME)
        .fallbackToDestructiveMigration()
        .addCallback(callback)
        .build()

    @Provides
    // @Singleton not needed, because this is always a singleton with Room
    fun provideNoteDao(db: NoteDatabase) = db.noteDao

    @ApplicationScope
    @Singleton
    @Provides
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

// Explicit ApplicationScope annotation in case we want to add more CoroutineScopes to app in future
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope
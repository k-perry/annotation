package dev.kevinperry.annotation.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.text.DateFormat

@Entity(tableName = "note_table")
@Parcelize
data class Note(
    val title: String = "",
    val content: String = "",
    val dateCreated: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Int = 0
) : Parcelable {

    val formattedDateCreated: String
        get() = DateFormat.getDateTimeInstance().format(dateCreated)

    val contentPreview: String
        get() = content.lines()[0]      // Get first line

}

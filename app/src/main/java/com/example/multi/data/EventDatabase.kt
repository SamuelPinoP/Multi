package com.example.multi.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import com.example.multi.Event
import com.example.multi.Note
import com.example.multi.TrashedNote
import com.example.multi.WeeklyGoal
import com.example.multi.WeeklyGoalRecord

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String,
    val date: String?
)

@Entity(tableName = "weekly_goals")
data class WeeklyGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val header: String,
    val frequency: Int,
    val remaining: Int,
    val lastCheckedDate: String?,
    val weekNumber: Int,
    val dayStates: String
)

@Dao
interface EventDao {
    @Query("SELECT * FROM events")
    suspend fun getEvents(): List<EventEntity>

    @Insert
    suspend fun insert(event: EventEntity): Long

    @Update
    suspend fun update(event: EventEntity)

    @Delete
    suspend fun delete(event: EventEntity)
}

@Dao
interface WeeklyGoalDao {
    @Query("SELECT * FROM weekly_goals")
    suspend fun getGoals(): List<WeeklyGoalEntity>

    @Insert
    suspend fun insert(goal: WeeklyGoalEntity): Long

    @Update
    suspend fun update(goal: WeeklyGoalEntity)

    @Delete
    suspend fun delete(goal: WeeklyGoalEntity)
}

@Entity(tableName = "weekly_goal_records")
data class WeeklyGoalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val header: String,
    val completed: Int,
    val frequency: Int,
    val weekStart: String,
    val weekEnd: String
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val header: String,
    val content: String,
    val created: Long,
    val lastOpened: Long,
    val scroll: Int = 0,
    val cursor: Int = 0
)

@Entity(tableName = "trashed_notes")
data class TrashedNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val header: String,
    val content: String,
    val created: Long,
    val deleted: Long
)

@Dao
interface WeeklyGoalRecordDao {
    @Query("SELECT * FROM weekly_goal_records ORDER BY weekStart DESC, id ASC")
    suspend fun getRecords(): List<WeeklyGoalRecordEntity>

    @Insert
    suspend fun insert(record: WeeklyGoalRecordEntity)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY lastOpened DESC")
    suspend fun getNotes(): List<NoteEntity>

    @Query("UPDATE notes SET lastOpened = :time WHERE id = :id")
    suspend fun touch(id: Long, time: Long)

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)
}

@Dao
interface TrashedNoteDao {
    @Query("SELECT * FROM trashed_notes ORDER BY deleted DESC")
    suspend fun getNotes(): List<TrashedNoteEntity>

    @Query("DELETE FROM trashed_notes WHERE deleted < :threshold")
    suspend fun deleteExpired(threshold: Long)

    @Insert
    suspend fun insert(note: TrashedNoteEntity): Long

    @Delete
    suspend fun delete(note: TrashedNoteEntity)
}

@Database(
    entities = [EventEntity::class, WeeklyGoalEntity::class, WeeklyGoalRecordEntity::class, NoteEntity::class, TrashedNoteEntity::class],
    version = 9
)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun weeklyGoalDao(): WeeklyGoalDao
    abstract fun weeklyGoalRecordDao(): WeeklyGoalRecordDao
    abstract fun noteDao(): NoteDao
    abstract fun trashedNoteDao(): TrashedNoteDao

    companion object {
        @Volatile
        private var INSTANCE: EventDatabase? = null

        fun getInstance(context: Context): EventDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EventDatabase::class.java,
                    "events.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

fun EventEntity.toModel() = Event(id, title, description, date)
fun Event.toEntity() = EventEntity(id, title, description, date)

fun WeeklyGoalEntity.toModel() =
    WeeklyGoal(id, header, frequency, remaining, lastCheckedDate, weekNumber, dayStates)

fun WeeklyGoal.toEntity() =
    WeeklyGoalEntity(id, header, frequency, remaining, lastCheckedDate, weekNumber, dayStates)

fun WeeklyGoalRecordEntity.toModel() = WeeklyGoalRecord(id, header, completed, frequency, weekStart, weekEnd)
fun WeeklyGoalRecord.toEntity() = WeeklyGoalRecordEntity(id, header, completed, frequency, weekStart, weekEnd)

fun NoteEntity.toModel() = Note(id, header, content, created, lastOpened, scroll, cursor)
fun Note.toEntity() = NoteEntity(id, header, content, created, lastOpened, scroll, cursor)

fun TrashedNoteEntity.toModel() = TrashedNote(id, header, content, created, deleted)
fun TrashedNote.toEntity() = TrashedNoteEntity(id, header, content, created, deleted)

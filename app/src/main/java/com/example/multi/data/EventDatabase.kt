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
    val weekNumber: Int
)

@Entity(tableName = "weekly_goal_records")
data class WeeklyGoalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val header: String,
    val frequency: Int,
    val completed: Int,
    val weekStart: String
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

@Dao
interface WeeklyGoalRecordDao {
    @Query("SELECT * FROM weekly_goal_records ORDER BY weekStart DESC, id")
    suspend fun getRecords(): List<WeeklyGoalRecordEntity>

    @Insert
    suspend fun insert(record: WeeklyGoalRecordEntity): Long
}

@Database(
    entities = [EventEntity::class, WeeklyGoalEntity::class, WeeklyGoalRecordEntity::class],
    version = 3
)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun weeklyGoalDao(): WeeklyGoalDao
    abstract fun weeklyGoalRecordDao(): WeeklyGoalRecordDao

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

fun WeeklyGoalEntity.toModel() = WeeklyGoal(id, header, frequency, remaining, lastCheckedDate, weekNumber)
fun WeeklyGoal.toEntity() = WeeklyGoalEntity(id, header, frequency, remaining, lastCheckedDate, weekNumber)

fun WeeklyGoalRecordEntity.toModel() = WeeklyGoalRecord(id, header, frequency, completed, weekStart)
fun WeeklyGoalRecord.toEntity() = WeeklyGoalRecordEntity(id, header, frequency, completed, weekStart)

package com.paras.airesumeanalyzer.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "analysis_history")
data class AnalysisRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val resumeText: String,
    val jobDescription: String,
    val timestamp: Long = System.currentTimeMillis(),
    val overallScore: Int,
    val jsonResult: String // Stringified ResumeAnalysisResponse JSON
)

@Dao
interface AnalysisRecordDao {
    @Query("SELECT * FROM analysis_history ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AnalysisRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AnalysisRecord)

    @Query("DELETE FROM analysis_history WHERE id = :id")
    suspend fun deleteRecordById(id: Int)

    @Query("DELETE FROM analysis_history")
    suspend fun clearAll()
}

@Database(entities = [AnalysisRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun analysisRecordDao(): AnalysisRecordDao
}

object DatabaseProvider {
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = androidx.room.Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "resume_analyser_database"
            )
            .fallbackToDestructiveMigration()
            .build()
            INSTANCE = instance
            instance
        }
    }
}

class AnalysisRepository(private val dao: AnalysisRecordDao) {
    val allRecords: Flow<List<AnalysisRecord>> = dao.getAllRecords()

    suspend fun insert(record: AnalysisRecord) {
        dao.insertRecord(record)
    }

    suspend fun deleteById(id: Int) {
        dao.deleteRecordById(id)
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }
}

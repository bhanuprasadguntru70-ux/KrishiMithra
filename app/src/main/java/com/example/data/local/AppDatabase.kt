package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AgriDao {
    // --- USER ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logoutAll()

    @Update
    suspend fun updateUser(user: UserEntity)

    // --- FARMS ---
    @Query("SELECT * FROM farms ORDER BY id DESC")
    fun getAllFarms(): Flow<List<FarmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarm(farm: FarmEntity)

    @Delete
    suspend fun deleteFarm(farm: FarmEntity)

    // --- DISEASE REPORTS ---
    @Query("SELECT * FROM disease_reports ORDER BY timestamp DESC")
    fun getAllDiseaseReports(): Flow<List<DiseaseReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiseaseReport(report: DiseaseReportEntity)

    @Query("DELETE FROM disease_reports WHERE id = :id")
    suspend fun deleteDiseaseReportById(id: Int)

    // --- MARKET CROPS ---
    @Query("SELECT * FROM market_crops ORDER BY cropName ASC")
    fun getAllMarketCrops(): Flow<List<MarketCropEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketCrops(crops: List<MarketCropEntity>)

    @Update
    suspend fun updateMarketCrop(crop: MarketCropEntity)

    @Query("DELETE FROM market_crops")
    suspend fun deleteAllMarketCrops()

    // --- NEWS ---
    @Query("SELECT * FROM news ORDER BY timestamp DESC")
    fun getAllNews(): Flow<List<NewsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(newsList: List<NewsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleNews(news: NewsEntity)

    @Query("DELETE FROM news")
    suspend fun deleteAllNews()

    // --- NOTIFICATIONS ---
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationAlertEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Int)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: Int)

    // --- TRANSACTIONS ---
    @Query("SELECT * FROM transactions WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getAllTransactionsForUser(email: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    // --- FARMING TIPS ---
    @Query("SELECT * FROM farming_tips ORDER BY timestamp DESC LIMIT 1")
    fun getLatestFarmingTip(): Flow<FarmingTipEntity?>

    @Query("SELECT * FROM farming_tips ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestFarmingTipSync(): FarmingTipEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmingTip(tip: FarmingTipEntity)

    // --- WEATHER CACHE ---
    @Query("SELECT * FROM weather_cache WHERE locationKey = :key LIMIT 1")
    suspend fun getWeatherCache(key: String): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherCache(cache: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache WHERE locationKey = :key")
    suspend fun deleteWeatherCache(key: String)

    @Query("DELETE FROM weather_cache")
    suspend fun clearAllWeatherCache()

    // --- CROP CALENDAR ---
    @Query("SELECT * FROM crop_calendars ORDER BY id DESC")
    fun getAllCropCalendars(): Flow<List<CropCalendarEntity>>

    @Query("SELECT * FROM crop_calendars WHERE id = :id LIMIT 1")
    suspend fun getCropCalendarById(id: Int): CropCalendarEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCropCalendar(calendar: CropCalendarEntity): Long

    @Delete
    suspend fun deleteCropCalendar(calendar: CropCalendarEntity)

    // --- CROP STAGES ---
    @Query("SELECT * FROM crop_stages WHERE calendarId = :calendarId ORDER BY startDay ASC")
    fun getStagesForCalendar(calendarId: Int): Flow<List<CropStageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCropStages(stages: List<CropStageEntity>)

    @Query("DELETE FROM crop_stages WHERE calendarId = :calendarId")
    suspend fun deleteStagesForCalendar(calendarId: Int)

    // --- CROP TASK REMINDERS ---
    @Query("SELECT * FROM crop_task_reminders WHERE calendarId = :calendarId ORDER BY dayOffset ASC")
    fun getTasksForCalendar(calendarId: Int): Flow<List<CropTaskReminderEntity>>

    @Query("SELECT * FROM crop_task_reminders ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<CropTaskReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCropTaskReminders(tasks: List<CropTaskReminderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleTaskReminder(task: CropTaskReminderEntity)

    @Update
    suspend fun updateTaskReminder(task: CropTaskReminderEntity)

    @Delete
    suspend fun deleteTaskReminder(task: CropTaskReminderEntity)

    @Query("DELETE FROM crop_task_reminders WHERE calendarId = :calendarId")
    suspend fun deleteTasksForCalendar(calendarId: Int)

    // --- WOMEN FARMER STORIES ("మా మహిళా రైతుల స్ఫూర్తి కథలు") ---
    @Query("SELECT * FROM women_farmer_stories WHERE isApproved = 1 ORDER BY timestamp DESC")
    fun getAllApprovedWomenFarmerStories(): Flow<List<WomanFarmerStoryEntity>>

    @Query("SELECT * FROM women_farmer_stories WHERE isApproved = 0 ORDER BY timestamp DESC")
    fun getPendingWomenFarmerStories(): Flow<List<WomanFarmerStoryEntity>>

    @Query("SELECT * FROM women_farmer_stories WHERE isApproved = 1 ORDER BY timestamp DESC")
    suspend fun getApprovedStoriesListOnce(): List<WomanFarmerStoryEntity>

    @Query("SELECT * FROM women_farmer_stories WHERE isApproved = 1 AND isFeaturedToday = 1 LIMIT 1")
    fun getFeaturedTodayStory(): Flow<WomanFarmerStoryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWomanFarmerStory(story: WomanFarmerStoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWomenFarmerStories(stories: List<WomanFarmerStoryEntity>)

    @Update
    suspend fun updateWomanFarmerStory(story: WomanFarmerStoryEntity)

    @Delete
    suspend fun deleteWomanFarmerStory(story: WomanFarmerStoryEntity)

    @Query("UPDATE women_farmer_stories SET likeCount = likeCount + 1 WHERE id = :id")
    suspend fun incrementStoryLike(id: Int)

    @Query("UPDATE women_farmer_stories SET isApproved = 1 WHERE id = :id")
    suspend fun approveStory(id: Int)

    @Query("UPDATE women_farmer_stories SET isFeaturedToday = CASE WHEN id = :id THEN 1 ELSE 0 END WHERE isApproved = 1")
    suspend fun setFeaturedStory(id: Int)

    @Query("SELECT COUNT(*) FROM women_farmer_stories")
    suspend fun getWomenFarmerStoriesCount(): Int

    // --- SAVED CALCULATIONS ---
    @Query("SELECT * FROM saved_calculations ORDER BY timestamp DESC")
    fun getAllSavedCalculations(): Flow<List<SavedCalculationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedCalculation(calc: SavedCalculationEntity)

    @Query("DELETE FROM saved_calculations WHERE id = :id")
    suspend fun deleteSavedCalculationById(id: Int)

    @Query("DELETE FROM saved_calculations")
    suspend fun deleteAllSavedCalculations()

    // --- MONEY BOOK EXPENSES ---
    @Query("SELECT * FROM moneybook_expenses WHERE userEmail = :userEmail ORDER BY timestamp DESC")
    fun getAllExpensesForUser(userEmail: String): Flow<List<ExpenseEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntryEntity)

    @Query("DELETE FROM moneybook_expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Int)

    // --- MONEY BOOK INCOME ---
    @Query("SELECT * FROM moneybook_income WHERE userEmail = :userEmail ORDER BY timestamp DESC")
    fun getAllIncomeForUser(userEmail: String): Flow<List<IncomeEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntryEntity)

    @Query("DELETE FROM moneybook_income WHERE id = :id")
    suspend fun deleteIncomeById(id: Int)

    // --- FARMER FIELDS ---
    @Query("SELECT * FROM farmer_fields WHERE userEmail = :userEmail ORDER BY timestamp DESC")
    fun getAllFieldsForUser(userEmail: String): Flow<List<FarmerFieldEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertField(field: FarmerFieldEntity)

    @Query("DELETE FROM farmer_fields WHERE id = :id")
    suspend fun deleteFieldById(id: Int)
}

@Database(
    entities = [
        UserEntity::class,
        FarmEntity::class,
        DiseaseReportEntity::class,
        MarketCropEntity::class,
        NewsEntity::class,
        NotificationAlertEntity::class,
        TransactionEntity::class,
        FarmingTipEntity::class,
        WeatherCacheEntity::class,
        CropCalendarEntity::class,
        CropStageEntity::class,
        CropTaskReminderEntity::class,
        WomanFarmerStoryEntity::class,
        SavedCalculationEntity::class,
        ExpenseEntryEntity::class,
        IncomeEntryEntity::class,
        FarmerFieldEntity::class
    ],
    version = 13,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AgriDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "agri_ai_database"
                )
                .fallbackToDestructiveMigration()
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

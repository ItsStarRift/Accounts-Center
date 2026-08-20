package com.omerplt.accountmanager.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class AppWithAccountCount(
    val id: Long,
    val name: String,
    val category: AppCategory,
    val iconPath: String?,
    val accountCount: Int,
    val isFavorite: Boolean
)

@Dao
interface AppDao {

    @Query(
        """
        SELECT apps.id AS id, apps.name AS name, apps.category AS category, apps.iconPath AS iconPath,
               (SELECT COUNT(*) FROM accounts WHERE accounts.appId = apps.id) AS accountCount
        FROM apps
        ORDER BY apps.name COLLATE NOCASE ASC
        """
    )
    fun getAllAppsWithCount(): Flow<List<AppWithAccountCount>>

    @Query("SELECT * FROM apps WHERE id = :appId")
    fun getAppById(appId: Long): Flow<AppItem?>

    @Insert
    suspend fun insertApp(app: AppItem): Long

    @Update
    suspend fun updateApp(app: AppItem)

    @Delete
    suspend fun deleteApp(app: AppItem)

    @Query("SELECT * FROM apps")
    suspend fun getAllAppsOnce(): List<AppItem>

    @Insert
    suspend fun insertApps(apps: List<AppItem>)

    @Query("DELETE FROM apps")
    suspend fun clearApps()

    @Query("UPDATE apps SET isFavorite = :isFavorite WHERE id = :appId")
    suspend fun setFavorite(appId: Long, isFavorite: Boolean)
}

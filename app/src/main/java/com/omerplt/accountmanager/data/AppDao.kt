package com.omerplt.accountmanager.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Bir uygulama satırı, o uygulamaya ait hesap sayısıyla birlikte. */
data class AppWithAccountCount(
    val id: Long,
    val name: String,
    val category: AppCategory,
    val iconPath: String?,
    val accountCount: Int
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
}

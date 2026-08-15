package com.omerplt.accountmanager.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val database: AppDatabase) {

    fun getAllAppsWithCount(): Flow<List<AppWithAccountCount>> =
        database.appDao().getAllAppsWithCount()

    suspend fun addApp(name: String, category: AppCategory, iconPath: String?): Long {
        return database.appDao().insertApp(
            AppItem(name = name.trim(), category = category, iconPath = iconPath)
        )
    }

    suspend fun deleteApp(app: AppItem) = database.appDao().deleteApp(app)

    fun getAccountsForApp(appId: Long): Flow<List<AccountItem>> =
        database.accountDao().getAccountsForApp(appId)
}

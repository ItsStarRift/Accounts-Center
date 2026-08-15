package com.omerplt.accountmanager.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val database: AppDatabase) {

    fun getAllAppsWithCount(): Flow<List<AppWithAccountCount>> =
        database.appDao().getAllAppsWithCount()

    fun getAppById(appId: Long): Flow<AppItem?> =
        database.appDao().getAppById(appId)

    suspend fun addApp(name: String, category: AppCategory, iconPath: String?): Long {
        return database.appDao().insertApp(
            AppItem(name = name.trim(), category = category, iconPath = iconPath)
        )
    }

    suspend fun deleteApp(app: AppItem) = database.appDao().deleteApp(app)

    fun getAccountsForApp(appId: Long): Flow<List<AccountItem>> =
        database.accountDao().getAccountsForApp(appId)

    suspend fun addAccount(appId: Long, name: String, iconPath: String?): Long {
        return database.accountDao().insertAccount(
            AccountItem(appId = appId, name = name.trim(), iconPath = iconPath)
        )
    }

    suspend fun deleteAccount(account: AccountItem) = database.accountDao().deleteAccount(account)
}

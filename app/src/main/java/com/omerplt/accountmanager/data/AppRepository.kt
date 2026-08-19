package com.omerplt.accountmanager.data

import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

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
    suspend fun updateApp(app: AppItem) = database.appDao().updateApp(app)

    fun getAccountsForApp(appId: Long): Flow<List<AccountItem>> =
        database.accountDao().getAccountsForApp(appId)

    suspend fun addAccount(appId: Long, name: String, iconPath: String?): Long {
        return database.accountDao().insertAccount(
            AccountItem(appId = appId, name = name.trim(), iconPath = iconPath)
        )
    }

    suspend fun deleteAccount(account: AccountItem) = database.accountDao().deleteAccount(account)
    suspend fun updateAccount(account: AccountItem) = database.accountDao().updateAccount(account)

    fun getFieldsForAccount(accountId: Long): Flow<List<AccountField>> =
        database.accountFieldDao().getFieldsForAccount(accountId)

    suspend fun addField(accountId: Long, label: String, value: String, isCustomLabel: Boolean, orderIndex: Int): Long {
        return database.accountFieldDao().insertField(
            AccountField(
                accountId = accountId,
                label = label.trim(),
                value = value.trim(),
                isCustomLabel = isCustomLabel,
                orderIndex = orderIndex
            )
        )
    }

    suspend fun deleteField(field: AccountField) = database.accountFieldDao().deleteField(field)

    suspend fun exportAllDataAsJson(): String {
        val root = JSONObject()

        val appsArray = JSONArray()
        database.appDao().getAllAppsOnce().forEach { app ->
            appsArray.put(
                JSONObject().apply {
                    put("id", app.id)
                    put("name", app.name)
                    put("category", app.category.name)
                    put("iconPath", app.iconPath ?: JSONObject.NULL)
                }
            )
        }

        val accountsArray = JSONArray()
        database.accountDao().getAllAccountsOnce().forEach { account ->
            accountsArray.put(
                JSONObject().apply {
                    put("id", account.id)
                    put("appId", account.appId)
                    put("name", account.name)
                    put("iconPath", account.iconPath ?: JSONObject.NULL)
                }
            )
        }

        val fieldsArray = JSONArray()
        database.accountFieldDao().getAllFieldsOnce().forEach { field ->
            fieldsArray.put(
                JSONObject().apply {
                    put("id", field.id)
                    put("accountId", field.accountId)
                    put("label", field.label)
                    put("value", field.value)
                    put("isCustomLabel", field.isCustomLabel)
                    put("orderIndex", field.orderIndex)
                }
            )
        }

        root.put("apps", appsArray)
        root.put("accounts", accountsArray)
        root.put("fields", fieldsArray)
        return root.toString(2)
    }

    suspend fun importAllDataFromJson(json: String) {
        val root = JSONObject(json)

        val apps = mutableListOf<AppItem>()
        val appsArray = root.optJSONArray("apps") ?: JSONArray()
        for (i in 0 until appsArray.length()) {
            val obj = appsArray.getJSONObject(i)
            apps.add(
                AppItem(
                    id = obj.getLong("id"),
                    name = obj.getString("name"),
                    category = AppCategory.valueOf(obj.getString("category")),
                    iconPath = if (obj.isNull("iconPath")) null else obj.getString("iconPath")
                )
            )
        }

        val accounts = mutableListOf<AccountItem>()
        val accountsArray = root.optJSONArray("accounts") ?: JSONArray()
        for (i in 0 until accountsArray.length()) {
            val obj = accountsArray.getJSONObject(i)
            accounts.add(
                AccountItem(
                    id = obj.getLong("id"),
                    appId = obj.getLong("appId"),
                    name = obj.getString("name"),
                    iconPath = if (obj.isNull("iconPath")) null else obj.getString("iconPath")
                )
            )
        }

        val fields = mutableListOf<AccountField>()
        val fieldsArray = root.optJSONArray("fields") ?: JSONArray()
        for (i in 0 until fieldsArray.length()) {
            val obj = fieldsArray.getJSONObject(i)
            fields.add(
                AccountField(
                    id = obj.getLong("id"),
                    accountId = obj.getLong("accountId"),
                    label = obj.getString("label"),
                    value = obj.getString("value"),
                    isCustomLabel = obj.getBoolean("isCustomLabel"),
                    orderIndex = obj.getInt("orderIndex")
                )
            )
        }

        database.appDao().clearApps()
        database.appDao().insertApps(apps)
        database.accountDao().insertAccounts(accounts)
        database.accountFieldDao().insertFields(fields)
    }
}

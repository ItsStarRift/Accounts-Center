package com.omerplt.accountmanager.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE appId = :appId ORDER BY name COLLATE NOCASE ASC")
    fun getAccountsForApp(appId: Long): Flow<List<AccountItem>>

    @Insert
    suspend fun insertAccount(account: AccountItem): Long

    @Update
    suspend fun updateAccount(account: AccountItem)

    @Delete
    suspend fun deleteAccount(account: AccountItem)

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccountsOnce(): List<AccountItem>

    @Insert
    suspend fun insertAccounts(accounts: List<AccountItem>)
}

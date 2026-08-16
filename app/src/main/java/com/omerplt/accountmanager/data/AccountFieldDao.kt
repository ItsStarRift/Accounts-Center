package com.omerplt.accountmanager.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountFieldDao {

    @Query("SELECT * FROM account_fields WHERE accountId = :accountId ORDER BY orderIndex ASC")
    fun getFieldsForAccount(accountId: Long): Flow<List<AccountField>>

    @Insert
    suspend fun insertField(field: AccountField): Long

    @Update
    suspend fun updateField(field: AccountField)

    @Delete
    suspend fun deleteField(field: AccountField)

    @Query("SELECT * FROM account_fields")
    suspend fun getAllFieldsOnce(): List<AccountField>

    @Insert
    suspend fun insertFields(fields: List<AccountField>)
}

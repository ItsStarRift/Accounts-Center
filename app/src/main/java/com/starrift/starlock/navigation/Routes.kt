package com.starrift.starlock.navigation

object Routes {
    const val HOME = "home"
    const val ACCOUNT_LIST = "accounts/{appId}"
    const val ACCOUNT_DETAIL = "account_detail/{accountId}"
    const val TRASH = "trash"
    const val ARCHIVED = "archived"

    fun accountList(appId: Long) = "accounts/$appId"
    fun accountDetail(accountId: Long) = "account_detail/$accountId"
}

package com.omerplt.accountmanager.navigation

object Routes {
    const val HOME = "home"
    const val ACCOUNT_LIST = "accounts/{appId}"

    fun accountList(appId: Long) = "accounts/$appId"
}

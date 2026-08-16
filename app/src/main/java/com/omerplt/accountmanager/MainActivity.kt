package com.omerplt.accountmanager

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.omerplt.accountmanager.data.AppDatabase
import com.omerplt.accountmanager.data.AppRepository
import com.omerplt.accountmanager.navigation.Routes
import com.omerplt.accountmanager.ui.screens.AccountDetailScreen
import com.omerplt.accountmanager.ui.screens.AccountDetailViewModel
import com.omerplt.accountmanager.ui.screens.AccountDetailViewModelFactory
import com.omerplt.accountmanager.ui.screens.AccountListScreen
import com.omerplt.accountmanager.ui.screens.AccountListViewModel
import com.omerplt.accountmanager.ui.screens.AccountListViewModelFactory
import com.omerplt.accountmanager.ui.screens.HomeScreen
import com.omerplt.accountmanager.ui.screens.HomeViewModel
import com.omerplt.accountmanager.ui.screens.HomeViewModelFactory
import com.omerplt.accountmanager.ui.screens.SettingsScreen
import com.omerplt.accountmanager.ui.screens.SettingsViewModel
import com.omerplt.accountmanager.ui.screens.SettingsViewModelFactory
import com.omerplt.accountmanager.ui.theme.HesapYoneticisiTheme
import com.omerplt.accountmanager.util.PinManager
import com.omerplt.accountmanager.ui.screens.LockScreen
import java.util.Locale

class MainActivity : ComponentActivity() {
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Dil ayarını yükle
        val prefs = applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val savedLang = prefs.getString("app_lang", "system") ?: "system"
        
        if (savedLang != "system") {
            val locale = Locale(savedLang)
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }

        val database = AppDatabase.getInstance(applicationContext)
        val repository = AppRepository(database)
        
        val pinManager = PinManager(applicationContext)

        setContent {
            HesapYoneticisiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isUnlocked by remember { mutableStateOf(!pinManager.isPinSet()) }

                    if (isUnlocked) {
                        AppRoot(repository)
                    } else {
                        LockScreen(
                            pinManager = pinManager,
                            onUnlocked = { isUnlocked = true }
                        )
                    }
                }
            }
        }
    }
}

private const val TRANSITION_DURATION = 320

@Composable
private fun AppRoot(repository: AppRepository) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(
            route = Routes.HOME,
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(TRANSITION_DURATION)) +
                fadeOut(tween(TRANSITION_DURATION))
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(TRANSITION_DURATION)) +
                fadeIn(tween(TRANSITION_DURATION))
            }
        ) {
            val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository))
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(repository))
            HomeScreen(
                viewModel = homeViewModel,
                onAppClick = { appId -> navController.navigate(Routes.accountList(appId)) },
                settingsContent = { SettingsScreen(viewModel = settingsViewModel) }
            )
        }

        composable(
            route = Routes.ACCOUNT_LIST,
            arguments = listOf(navArgument("appId") { type = NavType.LongType }),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(TRANSITION_DURATION)) + fadeIn(tween(TRANSITION_DURATION))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(TRANSITION_DURATION)) + fadeOut(tween(TRANSITION_DURATION))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(TRANSITION_DURATION)) + fadeOut(tween(TRANSITION_DURATION))
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(TRANSITION_DURATION)) + fadeIn(tween(TRANSITION_DURATION))
            }
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getLong("appId") ?: 0L
            val accountListViewModel: AccountListViewModel = viewModel(
                factory = AccountListViewModelFactory(repository, appId)
            )
            AccountListScreen(
                viewModel = accountListViewModel,
                onBack = { navController.popBackStack() },
                onAccountClick = { accountId -> navController.navigate(Routes.accountDetail(accountId)) }
            )
        }

        composable(
            route = Routes.ACCOUNT_DETAIL,
            arguments = listOf(navArgument("accountId") { type = NavType.LongType }),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(TRANSITION_DURATION)) + fadeIn(tween(TRANSITION_DURATION))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(TRANSITION_DURATION)) + fadeOut(tween(TRANSITION_DURATION))
            }
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getLong("accountId") ?: 0L
            val accountDetailViewModel: AccountDetailViewModel = viewModel(
                factory = AccountDetailViewModelFactory(repository, accountId)
            )
            AccountDetailScreen(
                viewModel = accountDetailViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

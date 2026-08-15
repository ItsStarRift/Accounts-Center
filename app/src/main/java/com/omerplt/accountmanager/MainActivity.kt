package com.omerplt.accountmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.omerplt.accountmanager.data.AppDatabase
import com.omerplt.accountmanager.data.AppRepository
import com.omerplt.accountmanager.ui.screens.HomeScreen
import com.omerplt.accountmanager.ui.screens.HomeViewModel
import com.omerplt.accountmanager.ui.screens.HomeViewModelFactory
import com.omerplt.accountmanager.ui.theme.HesapYoneticisiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = AppRepository(database)

        setContent {
            HesapYoneticisiTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppRoot(repository)
                }
            }
        }
    }
}

@Composable
private fun AppRoot(repository: AppRepository) {
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository))

    // Hesap ekranı navigasyonu 2. aşamada eklenecek; şimdilik ana ekranı gösteriyoruz.
    HomeScreen(
        viewModel = homeViewModel,
        onAppClick = { appId ->
            // TODO (2. Aşama): hesap listesi ekranına git
        }
    )
}

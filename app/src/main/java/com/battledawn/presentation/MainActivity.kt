package com.battledawn.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.battledawn.presentation.navigation.BattleDawnNavHost
import com.battledawn.presentation.ui.theme.BattleDawnTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the Battle Dawn game
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BattleDawnTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BattleDawnNavHost()
                }
            }
        }
    }
}

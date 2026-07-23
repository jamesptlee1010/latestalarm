package com.james.mathwakealarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppRepository.initialise(this)
        enableEdgeToEdge()
        setContent {
            val appState by AppRepository.state
            TazAlarmTheme(appState.themeMode) {
                if (appState.onboardingComplete) {
                    TazAlarmApp(appState)
                } else {
                    OnboardingScreen(appState)
                }
            }
        }
    }
}

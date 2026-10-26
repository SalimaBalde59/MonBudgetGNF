package com.orangedigitalcenter.monbudget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.orangedigitalcenter.monbudget.ui.navigation.AppNavigation
import com.orangedigitalcenter.monbudget.ui.theme.MonBudgetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MonBudgetTheme {
                AppNavigation()
            }
        }
    }
}

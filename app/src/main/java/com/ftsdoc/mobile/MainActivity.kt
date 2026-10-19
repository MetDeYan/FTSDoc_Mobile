package com.ftsdoc.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.ftsdoc.mobile.ui.LocalAppContainer
import com.ftsdoc.mobile.ui.navigation.FtsDocRoot
import com.ftsdoc.mobile.ui.theme.FtsDocTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as FtsDocApplication).container
        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                FtsDocTheme {
                    FtsDocRoot()
                }
            }
        }
    }
}

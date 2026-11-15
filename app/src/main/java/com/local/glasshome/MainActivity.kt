package com.local.glasshome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.local.glasshome.glass.GlassTheme

/**
 * Home *candidate* only. MAIN + HOME + LEANBACK_LAUNCHER so it appears in Apps.
 * Does not replace stock Google TV Home.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlassTheme {
                HomeScreen()
            }
        }
    }
}

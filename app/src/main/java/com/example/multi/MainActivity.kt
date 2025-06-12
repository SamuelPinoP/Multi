package com.example.multi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Scaffold
import androidx.compose.ui.Modifier
import com.example.multi.ui.theme.MultiTheme

// Work with all 3 different Calendar API's
/*
   Create pop up message similar to Make Toast but instead of an ugly Make Toast a beautiful pop up.
   It will happen whenever a New Event is added or a New Weekly activity is added.
   It will say New Event added or New Weekly Activity added.
*/
// Fix dropdown issue in edit!

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MultiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MedallionScreen()
                    }
                }
            }
        }
    }
}

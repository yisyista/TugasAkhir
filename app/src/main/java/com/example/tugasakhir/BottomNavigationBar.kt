package com.example.tugasakhir

import android.content.Intent
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext

@Composable
fun BottomNavigationBar(currentScreen: String) {
    val context = LocalContext.current

    NavigationBar {
        // Item 1: MainActivity (Halaman Utama)
        NavigationBarItem(
            selected = currentScreen == "Main",
            onClick = {
                if (currentScreen != "Main") {
                    context.startActivity(Intent(context, MainActivity::class.java))
                }
            },
            icon = { androidx.compose.material3.Icon(painterResource(id = R.drawable.ic_main), contentDescription = "Main") },
            label = { Text("Main") }
        )
        // Item 2: BreathingExercise
        NavigationBarItem(
            selected = currentScreen == "Breathing",
            onClick = {
                if (currentScreen != "Breathing") {
                    context.startActivity(Intent(context, BreathingExercise::class.java))
                }
            },
            icon = { androidx.compose.material3.Icon(painterResource(id = R.drawable.ic_breathing), contentDescription = "Breathing") },
            label = { Text("Breathing") }
        )
        // Item 3: AnxietyGraphActivity
        NavigationBarItem(
            selected = currentScreen == "Graph",
            onClick = {
                if (currentScreen != "Graph") {
                    context.startActivity(Intent(context, AnxietyGraphActivity::class.java))
                }
            },
            icon = { androidx.compose.material3.Icon(painterResource(id = R.drawable.ic_graph), contentDescription = "Graph") },
            label = { Text("Graph") }
        )
        // Item 4: AnxietyLogActivity
        NavigationBarItem(
            selected = currentScreen == "Log",
            onClick = {
                if (currentScreen != "Log") {
                    context.startActivity(Intent(context, AnxietyLogActivity::class.java))
                }
            },
            icon = { androidx.compose.material3.Icon(painterResource(id = R.drawable.ic_log), contentDescription = "Log") },
            label = { Text("Log") }
        )
    }
}

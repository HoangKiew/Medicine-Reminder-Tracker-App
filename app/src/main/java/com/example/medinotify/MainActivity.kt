package com.example.medinotify

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

import com.example.medinotify.ui.navigation.MedinotifyApp
import com.example.medinotify.ui.theme.MedinotifyTheme

class MainActivity : ComponentActivity() {

    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            MedinotifyTheme(darkTheme = false) {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    this.navController = navController

                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = { isGranted ->
                            if (!isGranted) {
                                Toast.makeText(this, "Bạn cần cấp quyền để nhận thông báo uống thuốc!", Toast.LENGTH_LONG).show()
                            }
                        }
                    )

                    SideEffect {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }

                    MedinotifyApp(navController = navController)

                    LaunchedEffect(Unit) {
                        checkAndNavigate(intent, navController)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
        navController?.let {
            checkAndNavigate(intent, it)
        }
    }

    private fun checkAndNavigate(intent: Intent?, navController: NavHostController) {
        val navigateTo = intent?.getStringExtra("NAVIGATE_TO")

        if (navigateTo == "reminder_screen") {
            val id = intent?.getStringExtra("MEDICINE_ID") ?: ""
            val name = intent?.getStringExtra("MEDICINE_NAME") ?: ""
            val dosage = intent?.getStringExtra("MEDICINE_DOSAGE") ?: ""
            val time = intent?.getStringExtra("SCHEDULE_TIME") ?: ""

            val route = "reminder/$id/$name/$dosage/$time"

            try {
                navController.navigate(route) {
                    launchSingleTop = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
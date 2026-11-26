package com.prepflow.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.prepflow.app.ui.navigation.PrepFlowAppRoot
import com.prepflow.app.ui.theme.PrepFlowTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private val quickAddHomeworkSignal = MutableStateFlow(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        handleLaunchIntent(intent)

        val repository = (application as PrepFlowApplication).container.repository

        setContent {
            PrepFlowTheme(dynamicColor = true) {
                val signal by quickAddHomeworkSignal.collectAsState()
                PrepFlowAppRoot(
                    repository = repository,
                    quickAddHomeworkSignal = signal,
                    onConsumeQuickAddSignal = { quickAddHomeworkSignal.value = 0 }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLaunchIntent(intent)
    }

    private fun handleLaunchIntent(intent: Intent?) {
        if (intent?.getStringExtra(EXTRA_WIDGET_ACTION) == ACTION_QUICK_ADD_HOMEWORK) {
            quickAddHomeworkSignal.value += 1
            intent.removeExtra(EXTRA_WIDGET_ACTION)
        }
    }

    companion object {
        const val EXTRA_WIDGET_ACTION = "extra_widget_action"
        const val ACTION_QUICK_ADD_HOMEWORK = "action_quick_add_homework"
    }
}

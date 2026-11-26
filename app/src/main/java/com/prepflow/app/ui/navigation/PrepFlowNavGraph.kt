package com.prepflow.app.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.prepflow.app.domain.repository.PrepFlowRepository
import com.prepflow.app.ui.academic.AcademicScreen
import com.prepflow.app.ui.components.FloatingBottomNavigationBar
import com.prepflow.app.ui.components.GlassAmbientBackground
import com.prepflow.app.ui.dashboard.DashboardScreen
import com.prepflow.app.ui.homework.HomeworkScreen
import com.prepflow.app.ui.schedule.ScheduleScreen

@Composable
fun PrepFlowAppRoot(
    repository: PrepFlowRepository,
    quickAddHomeworkSignal: Int,
    onConsumeQuickAddSignal: () -> Unit
) {
    var currentTab by rememberSaveable { mutableStateOf(PrepFlowTab.DASHBOARD) }
    var openHomeworkAddModal by rememberSaveable { mutableStateOf(false) }
    var selectedHomeworkIdForDetail by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(quickAddHomeworkSignal) {
        if (quickAddHomeworkSignal > 0) {
            currentTab = PrepFlowTab.HOMEWORK
            openHomeworkAddModal = true
            onConsumeQuickAddSignal()
        }
    }

    val navBarBottomDp = with(LocalDensity.current) {
        WindowInsets.navigationBars.getBottom(this).toDp()
    }
    val sharedContentPadding = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = 12.dp,
        bottom = navBarBottomDp + 108.dp
    )

    GlassAmbientBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(160))
                },
                label = "prepflow_tab_transition"
            ) { tab ->
                when (tab) {
                    PrepFlowTab.DASHBOARD -> {
                        DashboardScreen(
                            repository = repository,
                            onNavigateToTab = { target -> currentTab = target },
                            onOpenHomeworkAdd = {
                                currentTab = PrepFlowTab.HOMEWORK
                                openHomeworkAddModal = true
                            },
                            onOpenHomeworkDetail = { hwId ->
                                selectedHomeworkIdForDetail = hwId
                                currentTab = PrepFlowTab.HOMEWORK
                            },
                            contentPadding = sharedContentPadding
                        )
                    }

                    PrepFlowTab.HOMEWORK -> {
                        HomeworkScreen(
                            repository = repository,
                            openAddTrigger = openHomeworkAddModal,
                            onConsumeOpenAddTrigger = { openHomeworkAddModal = false },
                            initialSelectedHomeworkId = selectedHomeworkIdForDetail,
                            onConsumeSelectedHomeworkId = { selectedHomeworkIdForDetail = null },
                            contentPadding = sharedContentPadding
                        )
                    }

                    PrepFlowTab.SCHEDULE -> {
                        ScheduleScreen(
                            repository = repository,
                            contentPadding = sharedContentPadding
                        )
                    }

                    PrepFlowTab.ACADEMIC -> {
                        AcademicScreen(
                            repository = repository,
                            contentPadding = sharedContentPadding
                        )
                    }
                }
            }

            FloatingBottomNavigationBar(
                currentTab = currentTab,
                onSelectTab = { currentTab = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

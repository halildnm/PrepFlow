package com.prepflow.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.prepflow.app.data.local.entity.ExamEntity
import com.prepflow.app.data.local.entity.HomeworkWithDetails
import com.prepflow.app.data.local.entity.ResourceEntity
import com.prepflow.app.data.local.entity.ScheduleSessionEntity
import com.prepflow.app.domain.model.HomeworkStatus
import com.prepflow.app.domain.repository.PrepFlowRepository
import com.prepflow.app.ui.components.DateTimeUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

enum class SessionHighlightState {
    ONGOING,
    NEXT,
    UPCOMING,
    FINISHED
}

data class TodaySessionUiModel(
    val session: ScheduleSessionEntity,
    val state: SessionHighlightState
)

data class DashboardUiState(
    val greeting: String = DateTimeUtils.greetingForHour(),
    val formattedDate: String = DateTimeUtils.formatTodayHeader(),
    val todaySessions: List<TodaySessionUiModel> = emptyList(),
    val urgentAndUpcomingHomeworks: List<HomeworkWithDetails> = emptyList(),
    val overdueCount: Int = 0,
    val activeHomeworkCount: Int = 0,
    val nearestExam: ExamEntity? = null,
    val resourcesPreview: List<ResourceEntity> = emptyList(),
    val customTagColors: Map<String, Long> = emptyMap(),
    val isCompletelyEmpty: Boolean = false
)

class DashboardViewModel(
    private val repository: PrepFlowRepository
) : ViewModel() {

    private val todayDayOfWeek: Int = LocalDate.now().dayOfWeek.value

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observeSessionsForDay(todayDayOfWeek),
        repository.observeAllHomeworks(),
        repository.observeAllExams(),
        repository.observeAllResources(),
        repository.observeCustomSkillTags()
    ) { todaySessions, allHomeworks, allExams, allResources, customTags ->
        val nowMillis = System.currentTimeMillis()
        val nowTime = LocalTime.now()
        val today = LocalDate.now()

        val sortedSessions = todaySessions.sortedBy { it.startTime }
        var nextAssigned = false
        val sessionModels = sortedSessions.map { session ->
            val start = DateTimeUtils.parseLocalTimeOrNull(session.startTime)
            val end = DateTimeUtils.parseLocalTimeOrNull(session.endTime)
            val state = when {
                start != null && end != null && !nowTime.isBefore(start) && nowTime.isBefore(end) -> {
                    SessionHighlightState.ONGOING
                }
                end != null && !nowTime.isBefore(end) -> {
                    SessionHighlightState.FINISHED
                }
                !nextAssigned -> {
                    nextAssigned = true
                    SessionHighlightState.NEXT
                }
                else -> SessionHighlightState.UPCOMING
            }
            TodaySessionUiModel(session = session, state = state)
        }

        val activeHomeworks = allHomeworks
            .filter { it.homework.status != HomeworkStatus.DONE }
            .sortedWith(
                compareBy<HomeworkWithDetails> { it.homework.dueDateTime }
                    .thenByDescending { it.homework.priority.sortWeight }
            )

        val overdueCount = activeHomeworks.count { DateTimeUtils.isOverdue(it.homework.dueDateTime, nowMillis) }

        val nearestExam = allExams
            .filter { !DateTimeUtils.epochMillisToLocalDate(it.date).isBefore(today) }
            .minByOrNull { it.date }

        val tagColorMap = customTags.associate { it.name to it.colorHex }

        val isEmpty = todaySessions.isEmpty() &&
            allHomeworks.isEmpty() &&
            allExams.isEmpty() &&
            allResources.isEmpty()

        DashboardUiState(
            greeting = DateTimeUtils.greetingForHour(nowTime.hour),
            formattedDate = DateTimeUtils.formatTodayHeader(today),
            todaySessions = sessionModels,
            urgentAndUpcomingHomeworks = activeHomeworks.take(5),
            overdueCount = overdueCount,
            activeHomeworkCount = activeHomeworks.size,
            nearestExam = nearestExam,
            resourcesPreview = allResources.take(4),
            customTagColors = tagColorMap,
            isCompletelyEmpty = isEmpty
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )

    fun markHomeworkDone(homeworkId: Long) {
        viewModelScope.launch {
            repository.updateHomeworkStatus(homeworkId, HomeworkStatus.DONE)
        }
    }

    fun loadDemoData() {
        viewModelScope.launch {
            repository.seedDemoData()
        }
    }

    class Factory(private val repository: PrepFlowRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository) as T
        }
    }
}

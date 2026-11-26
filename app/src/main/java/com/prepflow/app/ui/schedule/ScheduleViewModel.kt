package com.prepflow.app.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.prepflow.app.data.local.entity.ScheduleSessionEntity
import com.prepflow.app.domain.model.SkillTagDefaults
import com.prepflow.app.domain.model.SkillTagOption
import com.prepflow.app.domain.repository.PrepFlowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ScheduleUiState(
    val selectedDayOfWeek: Int = LocalDate.now().dayOfWeek.value.coerceIn(1, 5),
    val showWeekend: Boolean = LocalDate.now().dayOfWeek.value > 5,
    val sessionsByDay: Map<Int, List<ScheduleSessionEntity>> = emptyMap(),
    val selectedDaySessions: List<ScheduleSessionEntity> = emptyList(),
    val totalWeeklySessions: Int = 0,
    val availableSkillTags: List<SkillTagOption> = SkillTagDefaults.builtInTags,
    val customTagColors: Map<String, Long> = emptyMap()
)

class ScheduleViewModel(
    private val repository: PrepFlowRepository
) : ViewModel() {

    private data class ScheduleSelection(
        val selectedDay: Int,
        val showWeekend: Boolean
    )

    private val todayDow = LocalDate.now().dayOfWeek.value
    private val selectionFlow = MutableStateFlow(
        ScheduleSelection(
            selectedDay = todayDow,
            showWeekend = todayDow > 5
        )
    )

    val uiState: StateFlow<ScheduleUiState> = combine(
        repository.observeAllScheduleSessions(),
        repository.observeCustomSkillTags(),
        selectionFlow
    ) { allSessions, customTags, selection ->
        val customOptions = customTags.map {
            SkillTagOption(name = it.name, colorHex = it.colorHex, isCustom = true)
        }
        val availableTags = (SkillTagDefaults.builtInTags + customOptions)
            .distinctBy { it.name.lowercase() }
        val customColorMap = customTags.associate { it.name to it.colorHex }

        val grouped = allSessions
            .groupBy { it.dayOfWeek }
            .mapValues { (_, list) -> list.sortedBy { it.startTime } }

        val effectiveDay = if (!selection.showWeekend && selection.selectedDay > 5) {
            1
        } else {
            selection.selectedDay
        }

        ScheduleUiState(
            selectedDayOfWeek = effectiveDay,
            showWeekend = selection.showWeekend,
            sessionsByDay = grouped,
            selectedDaySessions = grouped[effectiveDay].orEmpty(),
            totalWeeklySessions = allSessions.size,
            availableSkillTags = availableTags,
            customTagColors = customColorMap
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScheduleUiState(
            selectedDayOfWeek = todayDow,
            showWeekend = todayDow > 5
        )
    )

    fun selectDay(dayOfWeek: Int) {
        selectionFlow.update { it.copy(selectedDay = dayOfWeek.coerceIn(1, 7)) }
    }

    fun toggleWeekendVisibility() {
        selectionFlow.update { current ->
            val nextShow = !current.showWeekend
            val nextDay = if (!nextShow && current.selectedDay > 5) 1 else current.selectedDay
            current.copy(showWeekend = nextShow, selectedDay = nextDay)
        }
    }

    fun saveSession(session: ScheduleSessionEntity) {
        viewModelScope.launch {
            repository.saveScheduleSession(session)
            if (session.dayOfWeek > 5) {
                selectionFlow.update { it.copy(showWeekend = true, selectedDay = session.dayOfWeek) }
            } else {
                selectionFlow.update { it.copy(selectedDay = session.dayOfWeek) }
            }
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteScheduleSession(sessionId)
        }
    }

    fun addCustomSkillTag(name: String, colorHex: Long) {
        viewModelScope.launch {
            repository.addCustomSkillTag(name, colorHex)
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
            return ScheduleViewModel(repository) as T
        }
    }
}

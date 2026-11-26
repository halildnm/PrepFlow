package com.prepflow.app.ui.homework

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.prepflow.app.data.local.entity.HomeworkEntity
import com.prepflow.app.data.local.entity.HomeworkLinkEntity
import com.prepflow.app.data.local.entity.HomeworkWithDetails
import com.prepflow.app.data.local.entity.SubtaskEntity
import com.prepflow.app.domain.model.HomeworkPriority
import com.prepflow.app.domain.model.HomeworkStatus
import com.prepflow.app.domain.model.SkillTagDefaults
import com.prepflow.app.domain.model.SkillTagOption
import com.prepflow.app.domain.repository.PrepFlowRepository
import com.prepflow.app.ui.components.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

enum class HomeworkViewMode(val labelTr: String) {
    LIST("Liste"),
    CALENDAR("Takvim")
}

data class HomeworkFilterState(
    val viewMode: HomeworkViewMode = HomeworkViewMode.LIST,
    val selectedSkillTag: String? = null,
    val selectedPriority: HomeworkPriority? = null,
    val selectedStatus: HomeworkStatus? = null,
    val displayedMonth: YearMonth = YearMonth.now(),
    val selectedCalendarDate: LocalDate = LocalDate.now()
)

data class HomeworkUiState(
    val filter: HomeworkFilterState = HomeworkFilterState(),
    val allHomeworks: List<HomeworkWithDetails> = emptyList(),
    val filteredListHomeworks: List<HomeworkWithDetails> = emptyList(),
    val calendarDateMap: Map<LocalDate, List<HomeworkWithDetails>> = emptyMap(),
    val selectedDayHomeworks: List<HomeworkWithDetails> = emptyList(),
    val availableSkillTags: List<SkillTagOption> = SkillTagDefaults.builtInTags,
    val customTagColors: Map<String, Long> = emptyMap()
)

class HomeworkViewModel(
    private val repository: PrepFlowRepository
) : ViewModel() {

    private val filterFlow = MutableStateFlow(HomeworkFilterState())

    val uiState: StateFlow<HomeworkUiState> = combine(
        repository.observeAllHomeworks(),
        repository.observeCustomSkillTags(),
        filterFlow
    ) { allHomeworks, customTags, filter ->
        val customOptions = customTags.map {
            SkillTagOption(name = it.name, colorHex = it.colorHex, isCustom = true)
        }
        val availableTags = (SkillTagDefaults.builtInTags + customOptions)
            .distinctBy { it.name.lowercase() }
        val customColorMap = customTags.associate { it.name to it.colorHex }

        val filtered = allHomeworks
            .filter { details ->
                val hw = details.homework
                val skillMatch = filter.selectedSkillTag == null ||
                    hw.skillTag.equals(filter.selectedSkillTag, ignoreCase = true)
                val priorityMatch = filter.selectedPriority == null || hw.priority == filter.selectedPriority
                val statusMatch = filter.selectedStatus == null || hw.status == filter.selectedStatus
                skillMatch && priorityMatch && statusMatch
            }
            .sortedWith(
                compareBy<HomeworkWithDetails> { it.homework.status == HomeworkStatus.DONE }
                    .thenBy { it.homework.dueDateTime }
                    .thenByDescending { it.homework.priority.sortWeight }
            )

        val dateMap = allHomeworks.groupBy { details ->
            DateTimeUtils.epochMillisToLocalDate(details.homework.dueDateTime)
        }
        val selectedDayItems = dateMap[filter.selectedCalendarDate]
            ?.sortedBy { it.homework.dueDateTime }
            ?: emptyList()

        HomeworkUiState(
            filter = filter,
            allHomeworks = allHomeworks,
            filteredListHomeworks = filtered,
            calendarDateMap = dateMap,
            selectedDayHomeworks = selectedDayItems,
            availableSkillTags = availableTags,
            customTagColors = customColorMap
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeworkUiState()
    )

    fun setViewMode(mode: HomeworkViewMode) {
        filterFlow.update { it.copy(viewMode = mode) }
    }

    fun toggleSkillFilter(tag: String?) {
        filterFlow.update { current ->
            current.copy(selectedSkillTag = if (current.selectedSkillTag == tag) null else tag)
        }
    }

    fun togglePriorityFilter(priority: HomeworkPriority?) {
        filterFlow.update { current ->
            current.copy(selectedPriority = if (current.selectedPriority == priority) null else priority)
        }
    }

    fun toggleStatusFilter(status: HomeworkStatus?) {
        filterFlow.update { current ->
            current.copy(selectedStatus = if (current.selectedStatus == status) null else status)
        }
    }

    fun clearAllFilters() {
        filterFlow.update {
            it.copy(
                selectedSkillTag = null,
                selectedPriority = null,
                selectedStatus = null
            )
        }
    }

    fun changeCalendarMonth(deltaMonths: Long) {
        filterFlow.update { it.copy(displayedMonth = it.displayedMonth.plusMonths(deltaMonths)) }
    }

    fun selectCalendarDate(date: LocalDate) {
        filterFlow.update {
            it.copy(
                selectedCalendarDate = date,
                displayedMonth = YearMonth.from(date)
            )
        }
    }

    fun advanceHomeworkStatus(homework: HomeworkEntity) {
        viewModelScope.launch {
            repository.updateHomeworkStatus(homework.id, homework.status.nextStatus())
        }
    }

    fun setHomeworkStatus(homeworkId: Long, status: HomeworkStatus) {
        viewModelScope.launch {
            repository.updateHomeworkStatus(homeworkId, status)
        }
    }

    fun toggleSubtask(subtaskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleSubtask(subtaskId, isCompleted)
        }
    }

    fun saveHomework(
        homework: HomeworkEntity,
        links: List<HomeworkLinkEntity>,
        subtasks: List<SubtaskEntity>
    ) {
        viewModelScope.launch {
            repository.saveHomeworkWithDetails(homework, links, subtasks)
        }
    }

    fun deleteHomework(homeworkId: Long) {
        viewModelScope.launch {
            repository.deleteHomework(homeworkId)
        }
    }

    fun addCustomSkillTag(name: String, colorHex: Long) {
        viewModelScope.launch {
            repository.addCustomSkillTag(name, colorHex)
        }
    }

    class Factory(private val repository: PrepFlowRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeworkViewModel(repository) as T
        }
    }
}

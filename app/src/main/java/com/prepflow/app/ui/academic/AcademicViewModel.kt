package com.prepflow.app.ui.academic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.prepflow.app.data.local.entity.ExamEntity
import com.prepflow.app.data.local.entity.ResourceEntity
import com.prepflow.app.domain.model.SkillTagDefaults
import com.prepflow.app.domain.model.SkillTagOption
import com.prepflow.app.domain.repository.PrepFlowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AcademicSubTab(val labelTr: String) {
    EXAMS("Sınavlar & Notlar"),
    RESOURCES("Kaynak Takibi")
}

data class GradeSummary(
    val weightedAverage: Double? = null,
    val gradedWeightTotal: Double = 0.0,
    val totalPlannedWeight: Double = 0.0,
    val weightedEarnedPoints: Double = 0.0,
    val gradedExamCount: Int = 0,
    val totalExamCount: Int = 0
)

data class AcademicUiState(
    val selectedSubTab: AcademicSubTab = AcademicSubTab.EXAMS,
    val exams: List<ExamEntity> = emptyList(),
    val gradeSummary: GradeSummary = GradeSummary(),
    val resources: List<ResourceEntity> = emptyList(),
    val availableSkillTags: List<SkillTagOption> = SkillTagDefaults.builtInTags,
    val customTagColors: Map<String, Long> = emptyMap()
)

class AcademicViewModel(
    private val repository: PrepFlowRepository
) : ViewModel() {

    private val selectedSubTabFlow = MutableStateFlow(AcademicSubTab.EXAMS)

    val uiState: StateFlow<AcademicUiState> = combine(
        repository.observeAllExams(),
        repository.observeAllResources(),
        repository.observeCustomSkillTags(),
        selectedSubTabFlow
    ) { exams, resources, customTags, subTab ->
        val customOptions = customTags.map {
            SkillTagOption(name = it.name, colorHex = it.colorHex, isCustom = true)
        }
        val availableTags = (SkillTagDefaults.builtInTags + customOptions)
            .distinctBy { it.name.lowercase() }
        val customColorMap = customTags.associate { it.name to it.colorHex }

        val gradedExams = exams.filter { it.score != null && it.weightPercent > 0.0 }
        val gradedWeightSum = gradedExams.sumOf { it.weightPercent }
        val totalPlannedWeight = exams.sumOf { it.weightPercent }
        val weightedNumerator = gradedExams.sumOf { (it.score ?: 0.0) * it.weightPercent }

        val weightedAverage = if (gradedWeightSum > 0.0) {
            weightedNumerator / gradedWeightSum
        } else {
            null
        }
        val earnedPoints = weightedNumerator / 100.0

        val summary = GradeSummary(
            weightedAverage = weightedAverage,
            gradedWeightTotal = gradedWeightSum,
            totalPlannedWeight = totalPlannedWeight,
            weightedEarnedPoints = earnedPoints,
            gradedExamCount = gradedExams.size,
            totalExamCount = exams.size
        )

        AcademicUiState(
            selectedSubTab = subTab,
            exams = exams.sortedBy { it.date },
            gradeSummary = summary,
            resources = resources,
            availableSkillTags = availableTags,
            customTagColors = customColorMap
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AcademicUiState()
    )

    fun selectSubTab(tab: AcademicSubTab) {
        selectedSubTabFlow.value = tab
    }

    fun saveExam(exam: ExamEntity) {
        viewModelScope.launch {
            repository.saveExam(exam)
        }
    }

    fun deleteExam(examId: Long) {
        viewModelScope.launch {
            repository.deleteExam(examId)
        }
    }

    fun saveResource(resource: ResourceEntity) {
        viewModelScope.launch {
            repository.saveResource(resource)
        }
    }

    fun adjustResourceProgress(resource: ResourceEntity, delta: Int) {
        val nextCount = (resource.currentCount + delta).coerceIn(0, resource.totalCount)
        if (nextCount == resource.currentCount) return
        viewModelScope.launch {
            repository.updateResourceProgress(resource.id, nextCount)
        }
    }

    fun deleteResource(resourceId: Long) {
        viewModelScope.launch {
            repository.deleteResource(resourceId)
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
            return AcademicViewModel(repository) as T
        }
    }
}

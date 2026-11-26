package com.prepflow.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class HomeworkPriority(val labelTr: String, val sortWeight: Int) {
    URGENT("Acil", 3),
    MEDIUM("Orta", 2),
    LOW("Düşük", 1)
}

@Serializable
enum class HomeworkStatus(val labelTr: String) {
    TODO("Yapılacak"),
    IN_PROGRESS("Devam Ediyor"),
    DONE("Tamamlandı");

    fun nextStatus(): HomeworkStatus = when (this) {
        TODO -> IN_PROGRESS
        IN_PROGRESS -> DONE
        DONE -> TODO
    }
}

@Serializable
enum class ResourceUnitType(val labelTr: String, val pluralShortTr: String) {
    UNIT("Ünite", "ünite"),
    PAGE("Sayfa", "sayfa")
}

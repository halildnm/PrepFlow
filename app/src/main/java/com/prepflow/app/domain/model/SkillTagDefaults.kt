package com.prepflow.app.domain.model

data class SkillTagOption(
    val name: String,
    val colorHex: Long,
    val isCustom: Boolean = false
)

object SkillTagDefaults {
    val builtInTags: List<SkillTagOption> = listOf(
        SkillTagOption("Grammar", 0xFF818CF8),
        SkillTagOption("Reading", 0xFF38BDF8),
        SkillTagOption("Writing", 0xFFF472B6),
        SkillTagOption("Listening", 0xFF34D399),
        SkillTagOption("Speaking", 0xFFFBBF24),
        SkillTagOption("Vocabulary", 0xFFA78BFA)
    )

    val customColorPalette: List<Long> = listOf(
        0xFF2DD4BF,
        0xFFFB7185,
        0xFF60A5FA,
        0xFFC084FC,
        0xFFF97316,
        0xFF4ADE80
    )

    fun resolveColorHex(tagName: String, customTags: Map<String, Long> = emptyMap()): Long {
        builtInTags.firstOrNull { it.name.equals(tagName, ignoreCase = true) }?.let {
            return it.colorHex
        }
        customTags.entries.firstOrNull { it.key.equals(tagName, ignoreCase = true) }?.let {
            return it.value
        }
        val index = (tagName.lowercase().hashCode() and 0x7FFFFFFF) % customColorPalette.size
        return customColorPalette[index]
    }
}

package com.example.studenttaskmanager.data.model

/**
 * The eight subject hues from Design Brief section 4.2, stored as ARGB ints in
 * SQLite so a subject keeps its colour for its lifetime.
 */
object SubjectPalette {
    val INDIGO = 0xFF4F46E5.toInt()
    val TEAL = 0xFF00796B.toInt()
    val ROSE = 0xFFB03A64.toInt()
    val AMBER = 0xFFB45309.toInt()
    val FOREST = 0xFF2E6B3A.toInt()
    val SKY = 0xFF0369A1.toInt()
    val VIOLET = 0xFF7C3AED.toInt()
    val CORAL = 0xFFC2410C.toInt()

    val all: List<Int> = listOf(INDIGO, TEAL, ROSE, AMBER, FOREST, SKY, VIOLET, CORAL)

    /**
     * Starter subjects created with a new account so the first task form is
     * never empty. Colours cycle through the palette.
     */
    val starterNames: List<String> = listOf(
        "Mathematics", "Programming", "Capstone", "English", "Science", "History"
    )
}

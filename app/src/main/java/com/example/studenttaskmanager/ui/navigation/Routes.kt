package com.example.studenttaskmanager.ui.navigation

/**
 * String routes for the single-activity Navigation graph. The form route uses
 * an optional long argument: 0 means "add", any other id means "edit".
 */
object Routes {
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
    const val PROFILE = "profile"
    const val ABOUT = "about"

    const val TASK_FORM_ARG = "taskId"
    const val TASK_FORM = "task_form?$TASK_FORM_ARG={$TASK_FORM_ARG}"

    fun taskForm(taskId: Long = 0L): String = "task_form?$TASK_FORM_ARG=$taskId"
}

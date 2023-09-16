package dev.turingcomplete.intellijgradleutilitiesplugin.managegradleuserhome

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.DataContext
import dev.turingcomplete.intellijgradleutilitiesplugin.common.DeleteDirectoriesAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.Directory
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtils

class ClearGradleCachesAction
  : DeleteDirectoriesAction("Clear Gradle Caches", "Clears the Gradle caches.") {

  // -- Companion Object -------------------------------------------------------------------------------------------- //
  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    confirmationText = "Before clearing the Gradle caches all Gradle daemons " +
                       "should be terminated, otherwise the deletion might fail. " +
                       "Do you want to proceed?"
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun directories(dataContext: DataContext): List<Directory> {
    return listOf(Directory(GradleUtils.gradleUserHome().resolve("caches")))
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
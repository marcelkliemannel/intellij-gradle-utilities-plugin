package dev.turingcomplete.intellijgradleutilitiesplugin.managegradleuserhome.gradlewrapperdistribution

import com.intellij.openapi.actionSystem.ActionUpdateThread
import dev.turingcomplete.intellijgradleutilitiesplugin.common.CollectDirectoriesAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtils
import java.nio.file.Path

class CollectWrapperGradleDistributionsAction
  : CollectDirectoriesAction("Collect Gradle Distributions",
                             "Collecting Gradle daemon distributions...",
                             "Collects all downloaded Gradle distributions") {

  // -- Companion Object -------------------------------------------------------------------------------------------- //
  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    showOpensDialogIndicatorOnButtonText = false
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun parentDirectory(): Path = GradleUtils.gradleUserHome().resolve(GradleUtils.DISTRIBUTIONS_DIR)

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
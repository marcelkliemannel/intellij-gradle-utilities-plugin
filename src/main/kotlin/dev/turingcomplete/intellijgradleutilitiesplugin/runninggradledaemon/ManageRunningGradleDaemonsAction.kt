package dev.turingcomplete.intellijgradleutilitiesplugin.runninggradledaemon

import com.intellij.execution.runners.ExecutionUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.GradleUtilityDialog
import icons.GradleIcons
import java.awt.Dimension

open class ManageRunningGradleDaemonsAction
  : GradleUtilityAction<List<GradleDaemon>>("Manage Running Gradle Daemons",
                                            "Manages all running Gradle daemons.",
                                            ICON,
                                            executionMode = ExecutionMode.DIRECT) {

  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private val ICON = ExecutionUtil.getLiveIndicator(GradleIcons.Gradle, GradleIcons.Gradle.iconWidth, GradleIcons.Gradle.iconWidth)
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun runAction(executionContext: ExecutionContext, progressIndicator: ProgressIndicator) {
    // Nothing to do
  }

  override fun onSuccess(result: List<GradleDaemon>?, executionContext: ExecutionContext) {
    ApplicationManager.getApplication().invokeLater {
      GradleUtilityDialog.show("Running Gradle Daemons",
                               { RunningGradleDaemonsPanel() },
                               Dimension(500, 350),
                               executionContext.project)
    }
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
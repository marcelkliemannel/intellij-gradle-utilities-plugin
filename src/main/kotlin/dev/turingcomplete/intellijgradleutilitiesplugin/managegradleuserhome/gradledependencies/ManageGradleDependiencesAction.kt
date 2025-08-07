package dev.turingcomplete.intellijgradleutilitiesplugin.managegradleuserhome.gradledependencies

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.GradleUtilityDialog
import java.awt.Dimension

class ManageGradleDependiencesAction
  : GradleUtilityAction<Void>("Manage Gradle Dependencies",
                              "Manage all downloaded Gradle downloaded dependencies.",
                              executionMode = ExecutionMode.DIRECT) {

  // -- Companion Object -------------------------------------------------------------------------------------------- //
  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun runAction(executionContext: ExecutionContext, progressIndicator: ProgressIndicator) {
    // Nothing to do
  }

  override fun onSuccess(result: Void?, executionContext: ExecutionContext) {
    ApplicationManager.getApplication().invokeLater {
      GradleUtilityDialog.show("Manage Gradle Dependencies",
                               { GradleDependenciesPanel() },
                               Dimension(600, 350),
                               executionContext.project)
    }
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
package dev.turingcomplete.intellijgradleutilitiesplugin.managegradleuserhome.gradledaemoncache

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.GradleUtilityDialog
import java.awt.Dimension

class ManageGradleDaemonCachesAction :
  GradleUtilityAction<Void>(
    "Manage Gradle Daemon Caches",
    "Manage all Gradle daemon caches.",
    executionMode = ExecutionMode.DIRECT,
  ) {

  // -- Companion Object ---------------------------------------------------- //
  // -- Properties ---------------------------------------------------------- //
  // -- Initialization ------------------------------------------------------ //
  // -- Exported Methods ---------------------------------------------------- //

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun runAction(executionContext: ExecutionContext, progressIndicator: ProgressIndicator) {
    // Nothing to do
  }

  override fun onSuccess(result: Void?, executionContext: ExecutionContext) {
    ApplicationManager.getApplication().invokeLater {
      GradleUtilityDialog.show(
        "Manage Gradle Daemon Caches",
        { GradleDaemonCachesPanel() },
        Dimension(400, 350),
        executionContext.project,
      )
    }
  }

  // -- Private Methods ----------------------------------------------------- //
  // -- Inner Type ---------------------------------------------------------- //
}

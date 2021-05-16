package dev.turingcomplete.intellijgradleutilitiesplugin.managegradleuserhome.gradlewrapperdistribution

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.GradleUtilityDialog
import java.awt.Dimension

class ManageGradleWrapperDistributionsAction
  : GradleUtilityAction<Void>("Manage Gradle Wrapper Distributions",
                              "Manage all downloaded Gradle wrapper distributions.",
                              executionMode = ExecutionMode.DIRECT) {

  // -- Companion Object -------------------------------------------------------------------------------------------- //
  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun runAction(executionContext: ExecutionContext, progressIndicator: ProgressIndicator) {
    // Nothing to do
  }

  override fun onSuccess(result: Void?, executionContext: ExecutionContext) {
    ApplicationManager.getApplication().invokeLater {
      GradleUtilityDialog.show("Manage Gradle Wrapper Distributions",
                                    GradleWrapperDistributionsPanel(),
                                    Dimension(400, 350),
                                    executionContext.project)
    }
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
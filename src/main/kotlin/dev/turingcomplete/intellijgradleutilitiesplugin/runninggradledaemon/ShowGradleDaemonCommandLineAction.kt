package dev.turingcomplete.intellijgradleutilitiesplugin.runninggradledaemon

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.ui.popup.JBPopupFactory
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.UiUtils

class ShowGradleDaemonCommandLineAction
  : GradleUtilityAction<Void>("Show Command Line",
                              "Shows the command line that was used to start the selected Gradle daemon.",
                              AllIcons.Nodes.Console,
                              executionMode = ExecutionMode.DIRECT) {

  // -- Companion Object -------------------------------------------------------------------------------------------- //
  // -- Variables --------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    isVisible = { e -> RunningGradleDaemonsPanel.SELECTED_DAEMON.getData(e.dataContext) != null }
  }

  // -- Exported Methods -------------------------------------------------------------------------------------------- //

  override fun runAction(executionContext: ExecutionContext, progressIndicator: ProgressIndicator) {
    val gradleDaemon = RunningGradleDaemonsPanel.SELECTED_DAEMON.getData(executionContext.dataContext) ?: throw IllegalStateException("snh: Missing data")

    val commandLineText = gradleDaemon.processInfo.commandLine.replace(" -", " \\\n -")
    val commandLineTextAreaPanel = UiUtils.Component.TextAreaPanel(commandLineText)

    ApplicationManager.getApplication().invokeLater {
      JBPopupFactory.getInstance()
              .createComponentPopupBuilder(commandLineTextAreaPanel, commandLineTextAreaPanel)
              .setRequestFocus(true)
              .setTitle("Command Line of Gradle Daemon With PID ${gradleDaemon.processInfo.pid}")
              .setFocusable(true)
              .setResizable(true)
              .setMovable(true)
              .setModalContext(false)
              .setShowShadow(true)
              .setShowBorder(false)
              .setCancelKeyEnabled(true)
              .setCancelOnClickOutside(true)
              .setCancelOnOtherWindowOpen(false)
              .createPopup()
              .showInBestPositionFor(executionContext.dataContext)
    }
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
}
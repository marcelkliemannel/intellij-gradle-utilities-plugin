package dev.turingcomplete.intellijgradleutilitiesplugin.runninggradledaemon

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.vfs.VirtualFileManager
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityActionFailedException
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtils
import java.nio.file.Path

class OpenGradleDaemonLogAction :
  GradleUtilityAction<Void>(
    "Open Daemon Log",
    icon = AllIcons.FileTypes.Text,
    executionMode = ExecutionMode.DIRECT,
  ) {

  // -- Companion Object ---------------------------------------------------- //
  // -- Properties ---------------------------------------------------------- //
  // -- Initialization ------------------------------------------------------ //

  init {
    showOpensDialogIndicatorOnButtonText = false

    isVisible = { dataContext ->
      val gradleDaemon = RunningGradleDaemonsPanel.SELECTED_DAEMON.getData(dataContext)
      gradleDaemon?.version != null
    }
  }

  // -- Exported Methods ---------------------------------------------------- //

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun runAction(executionContext: ExecutionContext, progressIndicator: ProgressIndicator) {
    executionContext.project ?: return

    val gradleDaemon =
      RunningGradleDaemonsPanel.SELECTED_DAEMON.getData(executionContext.dataContext)
        ?: throw IllegalStateException("snh: Missing data")
    gradleDaemon.version ?: return

    val gradleHome = GradleUtils.gradleUserHome()
    val logFile =
      gradleHome.resolve(
        Path.of("daemon", gradleDaemon.version, "daemon-${gradleDaemon.pid}.out.log")
      )
    val daemonLogFile =
      VirtualFileManager.getInstance().findFileByNioPath(logFile)
        ?: throw GradleUtilityActionFailedException(
          "Couldn't find Gradle daemon log file: $logFile"
        )

    ApplicationManager.getApplication().invokeLaterOnWriteThread {
      FileEditorManager.getInstance(executionContext.project).openFile(daemonLogFile, true)
    }
  }

  // -- Private Methods ----------------------------------------------------- //
  // -- Inner Type ---------------------------------------------------------- //
}

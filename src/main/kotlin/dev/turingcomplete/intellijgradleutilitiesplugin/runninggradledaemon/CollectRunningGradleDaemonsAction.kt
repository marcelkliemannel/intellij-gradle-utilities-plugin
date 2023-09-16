package dev.turingcomplete.intellijgradleutilitiesplugin.runninggradledaemon

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.guessProjectDir
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtils
import java.nio.file.Path
import kotlin.streams.asSequence

class CollectRunningGradleDaemonsAction
  : GradleUtilityAction<List<GradleDaemon>>("Collect Running Gradle Daemons",
                                            "Collects all running Gradle daemons.",
                                            AllIcons.Actions.Refresh,
                                            executionMode = ExecutionMode.MODAL) {

  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private const val GRADLE_DAEMON_CLASS = "org.gradle.launcher.daemon.bootstrap.GradleDaemon"
    private val LOGGER = Logger.getInstance(GradleUtilityAction::class.java)

    val DETERMINE_DAEMON_STATUS: DataKey<Boolean> = DataKey.create("Gradle.Utilities.Plugin.CollectRunningGradleDaemonsAction.FindDaemonStatus")
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //
  
  init {
    showOpensDialogIndicatorOnButtonText = false
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun runAction(executionContext: ExecutionContext, progressIndicator: ProgressIndicator) {
    // Collect processes
    progressIndicator.text = "Collecting Gradle daemon processes..."

    val gradleDaemonProcesses: Map<Long, ProcessHandle> = ProcessHandle.allProcesses()
            .asSequence()
            .filter { process ->
              return@filter try {
                process.info().commandLine()
                        .map { it.contains(GRADLE_DAEMON_CLASS) }
                        .orElse(false)
              }
              catch (e: Exception) {
                LOGGER.warn("Failed to read command line of process with PID ${process.pid()} to check if it is a Gradle daemon process.", e)
                false
              }
            }.associateBy { it.pid() }

    val gradleDaemons = mutableMapOf<Long, GradleDaemon>()

    // Add daemons with known status
    progressIndicator.checkCanceled()

    if (DETERMINE_DAEMON_STATUS.getData(executionContext.dataContext) == true) {
      progressIndicator.text = "Determining Gradle daemon statuses..."

      val workingDirToGradleExecutable = mutableMapOf<Path?, Path>()

      executionContext.project?.guessProjectDir()?.let { projectDir ->
        progressIndicator.checkCanceled()

        GradleUtils.findGradlewExecutable(projectDir)?.let { gradlewExecutable ->
          workingDirToGradleExecutable.put(projectDir.toNioPath(), gradlewExecutable.toNioPath())
        }
      }

      GradleUtils.findSystemGradleExecutable(executionContext.project)?.let { systemGradleExecutable ->
        workingDirToGradleExecutable.put(null, systemGradleExecutable)
      }

      workingDirToGradleExecutable.forEach { (workingDir, gradleExecutable) ->
        progressIndicator.checkCanceled()
        GradleUtils.determineGradleDaemonStatus(workingDir, gradleExecutable).forEach { (pid, status) ->
          if (gradleDaemonProcesses.containsKey(pid)) {
            gradleDaemons[pid] = GradleDaemon(gradleDaemonProcesses[pid]!!, status)
          }
        }
      }
    }

    progressIndicator.checkCanceled()

    // Add remaining Gradle daemons, which have an unknown status
    gradleDaemonProcesses.filter { !gradleDaemons.containsKey(it.key) }.forEach {
      gradleDaemons[it.key] = GradleDaemon(it.value, null)
    }

    result(gradleDaemons.values.toList())
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
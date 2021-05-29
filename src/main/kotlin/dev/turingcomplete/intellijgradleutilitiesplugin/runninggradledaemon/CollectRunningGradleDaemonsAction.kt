package dev.turingcomplete.intellijgradleutilitiesplugin.runninggradledaemon

import com.intellij.execution.process.impl.ProcessListUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.guessProjectDir
import com.intellij.util.containers.orNull
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtils
import java.nio.file.Path

class CollectRunningGradleDaemonsAction
  : GradleUtilityAction<List<GradleDaemon>>("Collect Running Gradle Daemons",
                                            "Collects all running Gradle daemons.",
                                            AllIcons.Actions.Refresh,
                                            executionMode = ExecutionMode.MODAL) {

  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private const val GRADLE_DAEMON_CLASS = "org.gradle.launcher.daemon.bootstrap.GradleDaemon"

    val DETERMINE_DAEMON_STATUS: DataKey<Boolean> = DataKey.create("Gradle.Utilities.Plugin.CollectRunningGradleDaemonsAction.FindDaemonStatus")
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //
  
  init {
    showOpensDialogIndicatorOnButtonText = false
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun runAction(executionContext: ExecutionContext, progressIndicator: ProgressIndicator) {
    // Collect processes
    progressIndicator.text = "Collecting Gradle daemon processes..."
    val gradleDaemonProcesses = ProcessListUtil.getProcessList()
            .filter { it.commandLine.contains(GRADLE_DAEMON_CLASS) }
            .associateBy { it.pid }

    val gradleDaemons = mutableMapOf<Int, GradleDaemon>()

    // Add Daemons with known status
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
        GradleUtils.determineGradleDaemonStatus(workingDir, gradleExecutable)
                .forEach { (pid, status) ->
                  if (gradleDaemonProcesses.containsKey(pid)) {
                    val processHandle = ProcessHandle.of(pid.toLong()).orNull()
                    gradleDaemons[pid] = GradleDaemon(gradleDaemonProcesses[pid]!!, processHandle, status)
                  }
                }
      }
    }

    progressIndicator.checkCanceled()
    // Add reaming Gradle Daemons, which have an unknown status
    gradleDaemonProcesses.filter { !gradleDaemons.containsKey(it.key) }
            .forEach {
              val processHandle = ProcessHandle.of(it.value.pid.toLong()).orNull()
              gradleDaemons[it.key] = GradleDaemon(it.value, processHandle, null)
            }

    result(gradleDaemons.values.toList())
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
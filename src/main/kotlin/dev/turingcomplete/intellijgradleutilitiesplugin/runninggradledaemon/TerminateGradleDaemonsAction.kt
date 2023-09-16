package dev.turingcomplete.intellijgradleutilitiesplugin.runninggradledaemon

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.DumbAware
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityActionFailedException
import javax.swing.Icon

abstract class TerminateGradleDaemonsAction private constructor(private val gradleDaemonsDataKey: DataKey<List<GradleDaemon>>)
  : GradleUtilityAction<Void>("Terminate Gradle Daemons"), DumbAware {

  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private val LOG = Logger.getInstance(TerminateGradleDaemonsAction::class.java)
  }

  // -- Variables --------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    title = { _, e -> title(e.dataContext) }
    isVisible = { e -> gradleDaemons(e.dataContext).isNotEmpty() }
  }

  // -- Exported Methods -------------------------------------------------------------------------------------------- //

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    super.update(e)

    e.presentation.icon = icon()
  }

  override fun runAction(executionContext: ExecutionContext, progressIndicator: ProgressIndicator) {
    gradleDaemons(executionContext.dataContext).forEach { gradleDaemon ->
      progressIndicator.checkCanceled()
      try {
        progressIndicator.text = "Terminating Gradle daemon with PID ${gradleDaemon.pid}..."
        terminate(gradleDaemon, progressIndicator)
      }
      catch (e: Exception) {
        val errorMessage = errorMessage(gradleDaemon, e)
        addDeferredFailure(GradleUtilityActionFailedException(errorMessage, e))
      }
    }
  }

  protected abstract fun title(dataContext: DataContext): String

  protected abstract fun icon(): Icon

  protected abstract fun errorMessage(gradleDaemon: GradleDaemon, error: Exception): String

  protected abstract fun terminate(gradleDaemon: GradleDaemon, progressIndicator: ProgressIndicator)

  protected fun gradleDaemons(dataContext: DataContext): List<GradleDaemon> {
    return gradleDaemonsDataKey.getData(dataContext)
      ?: throw IllegalStateException("Data context is missing required data key '${gradleDaemonsDataKey.name}'.")
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  abstract class GracefullyTerminateGradleDaemonsAction(gradleDaemonsDataKey: DataKey<List<GradleDaemon>>)
    : TerminateGradleDaemonsAction(gradleDaemonsDataKey) {

    override fun icon(): Icon = AllIcons.Actions.Suspend

    override fun errorMessage(gradleDaemon: GradleDaemon, error: Exception): String {
      return "Failed to gracefully terminate Gradle daemon with PID ${gradleDaemon.pid}: ${error.message}"
    }

    override fun terminate(gradleDaemon: GradleDaemon, progressIndicator: ProgressIndicator) {
      val message = "Gracefully terminating Gradle daemon with PID ${gradleDaemon.pid}."
      progressIndicator.text2 = message
      LOG.info(message)

      gradleDaemon.terminateGracefully()
    }
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  class GracefullyAll : GracefullyTerminateGradleDaemonsAction(RunningGradleDaemonsPanel.ALL_DAEMONS) {

    override fun title(dataContext: DataContext): String = "Gracefully Terminate All Daemons"
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  class GracefullySelected : GracefullyTerminateGradleDaemonsAction(RunningGradleDaemonsPanel.SELECTED_DAEMONS) {

    override fun title(dataContext: DataContext): String {
      val numOfDaemons = gradleDaemons(dataContext).size
      return if (numOfDaemons == 1) "Gracefully Terminate Daemon" else "Gracefully Terminate $numOfDaemons Daemons"
    }
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  abstract class ForciblyTerminateGradleDaemonsAction(gradleDaemonsDataKey: DataKey<List<GradleDaemon>>)
    : TerminateGradleDaemonsAction(gradleDaemonsDataKey) {

    override fun icon(): Icon = AllIcons.Debugger.KillProcess

    override fun errorMessage(gradleDaemon: GradleDaemon, error: Exception): String {
      return "Failed to forcibly terminating Gradle daemon with PID ${gradleDaemon.pid}: ${error.message}"
    }

    override fun terminate(gradleDaemon: GradleDaemon, progressIndicator: ProgressIndicator) {
      val message = "Forcibly terminating Gradle daemon with PID ${gradleDaemon.pid}..."
      progressIndicator.text2 = message
      LOG.info(message)

      gradleDaemon.terminateForcibly()
    }
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  class ForciblyTerminateAll : ForciblyTerminateGradleDaemonsAction(RunningGradleDaemonsPanel.ALL_DAEMONS) {

    override fun title(dataContext: DataContext): String = "Forcibly Terminate All Gradle Daemons"
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  class ForciblyTerminateSelected : ForciblyTerminateGradleDaemonsAction(RunningGradleDaemonsPanel.SELECTED_DAEMONS) {

    override fun title(dataContext: DataContext): String {
      val numOfDaemons = gradleDaemons(dataContext).size
      return if (numOfDaemons == 1) "Forcibly Terminate Daemon" else "Forcibly Terminate $numOfDaemons Daemons"
    }
  }
}

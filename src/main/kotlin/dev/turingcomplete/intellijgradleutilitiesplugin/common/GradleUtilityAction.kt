@file:Suppress("DialogTitleCapitalization")

package dev.turingcomplete.intellijgradleutilitiesplugin.common

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.EdtReplacementThread
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.Messages.YES
import com.intellij.openapi.util.NlsActions
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.NotificationUtils
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.Icon
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
abstract class GradleUtilityAction<R>(initialTitle: @NlsActions.ActionText String,
                                      description: @NlsActions.ActionDescription String? = null,
                                      icon: Icon? = null,
                                      private val canBeCancelled: Boolean = true,
                                      private val executionMode: ExecutionMode = ExecutionMode.BACKGROUND,
                                      private val performInBackgroundOption: PerformInBackgroundOption = getDefaultPerformInBackgroundOption(executionMode))
  : AnAction(null, description, icon), DumbAware {

  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private val LOGGER = Logger.getInstance(GradleUtilityAction::class.java)

    private fun getDefaultPerformInBackgroundOption(executionMode: ExecutionMode) = when (executionMode) {
      ExecutionMode.BACKGROUND -> PerformInBackgroundOption.ALWAYS_BACKGROUND
      else -> PerformInBackgroundOption.DEAF
    }
  }

  // -- Variables --------------------------------------------------------------------------------------------------- //

  var title: (Boolean, AnActionEvent) -> String = { isExecutionTitle, e ->
    "$initialTitle${if (!isExecutionTitle && (showOpensDialogIndicatorOnButtonText || confirmationText != null)) "..." else ""}"
  }
  var showOpensDialogIndicatorOnButtonText: Boolean = true
  var failedTitle: (AnActionEvent) -> String = { e -> "Action \"${title(false, e)}\" failed" }
  var failureHandlingMode: FailureHandlingMode = FailureHandlingMode.AS_DIALOG
  var confirmationText: String? = null

  private val onBeforeStart: MutableList<() -> Unit> = mutableListOf()
  private val onSuccess: MutableList<(R?) -> Unit> = mutableListOf()
  private val onFinished: MutableList<() -> Unit> = mutableListOf()
  private val onFailure: MutableList<(Throwable) -> Unit> = mutableListOf()

  var isVisible: (AnActionEvent) -> Boolean = { true }
  var isEnabled: (AnActionEvent) -> Boolean = { true }

  private val inExecution = AtomicBoolean(false)
  private var result: R? = null
  private val failures = mutableListOf<Throwable>()

  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exported Methods -------------------------------------------------------------------------------------------- //

  final override fun actionPerformed(e: AnActionEvent) {
    if (inExecution.getAndSet(true)) {
      return
    }

    val executionContext = ExecutionContext(e.dataContext,
                                            CommonDataKeys.PROJECT.getData(e.dataContext),
                                            title(true, e),
                                            failedTitle(e))

    if (confirmationText != null) {
      val doRunAction = MessageDialogBuilder.yesNoCancel("Confirm", confirmationText!!).show(executionContext.project) == YES
      if (!doRunAction) {
        try {
          onCanceled0(executionContext)
        }
        finally {
          onFinished0(executionContext)
        }
        return
      }
    }

    onBeforeStart()
    onBeforeStart.forEach { it() }

    when (executionMode) {
      ExecutionMode.BACKGROUND -> BackgroundTask(canBeCancelled, this, executionContext, performInBackgroundOption).queue()
      ExecutionMode.MODAL -> ModalTask(canBeCancelled, this, executionContext, performInBackgroundOption).queue()
      ExecutionMode.DIRECT -> {
        try {
          runAction(executionContext, EmptyProgressIndicator())
          onSuccess0(executionContext)
        }
        catch (e: ProcessCanceledException) {
          onCanceled0(executionContext)
          throw e
        }
        catch (e: Throwable) {
          onThrowable0(e, executionContext)
        }
        finally {
          onFinished0(executionContext)
        }
      }
    }
  }

  /**
   * Overrider must call super method!
   */
  override fun update(e: AnActionEvent) {
    val inExecution = inExecution.get()
    e.presentation.text = title(inExecution, e)
    e.presentation.isEnabled = if (inExecution) false else isEnabled(e)
    e.presentation.isVisible = isVisible(e)
  }

  fun execute(dataContextComponent: JComponent?, presentation: Presentation? = templatePresentation) {
    val dataContext = DataManager.getInstance().getDataContext(dataContextComponent)
    val actionEvent = AnActionEvent.createFromDataContext(ActionPlaces.UNKNOWN, presentation, dataContext)
    update(actionEvent)
    actionPerformed(actionEvent)
  }

  abstract fun runAction(executionContext: ExecutionContext, progressIndicator: ProgressIndicator)

  protected fun addDeferredFailure(error: Throwable) {
    failures.add(error)
  }

  open fun onBeforeStart() {
    // Override if needed
  }

  fun onBeforeStart(onBeforeStart: () -> Unit): GradleUtilityAction<R> {
    this.onBeforeStart.add(onBeforeStart)
    return this
  }

  open fun onFinished() {
    // Override if needed
  }

  fun onFinished(onFinished: () -> Unit): GradleUtilityAction<R> {
    this.onFinished.add(onFinished)
    return this
  }

  fun result(value: R) {
    result = value
  }

  open fun onSuccess(result: R?, executionContext: ExecutionContext) {
    // Override if needed
  }

  fun onSuccess(onSuccess: (R?) -> Unit): GradleUtilityAction<R> {
    this.onSuccess.add(onSuccess)
    return this
  }

  fun onFailure(onFailure: (Throwable) -> Unit): GradleUtilityAction<R> {
    this.onFailure.add(onFailure)
    return this
  }

  fun isInExecution() = inExecution.get()

  // -- Private Methods --------------------------------------------------------------------------------------------- //

  private fun onThrowable0(failure: Throwable, executionContext: ExecutionContext) {
    failures.add(failure)
    handleFailures(executionContext)
  }

  private fun onSuccess0(executionContext: ExecutionContext) {
    if (failures.isNotEmpty()) {
      // Will be handled in onFinished()
      return
    }

    onSuccess(result, executionContext)
    onSuccess.forEach { it(result) }
  }

  private fun onFinished0(executionContext: ExecutionContext) {
    inExecution.set(false)

    handleFailures(executionContext)

    onFinished()
    onFinished.forEach { it() }
  }

  private fun onCanceled0(executionContext: ExecutionContext) {
    handleFailures(executionContext)
  }

  private fun handleFailures(executionContext: ExecutionContext) {
    failures.forEach { failure ->
      onFailure.forEach { it(failure) }
      handleFailure(failure, executionContext)
    }
  }

  private fun handleFailure(error: Throwable, executionContext: ExecutionContext) {
    val (message, displayMessage) = when (error is GradleUtilityActionFailedException) {
      true -> Pair(error.message, error.message)
      false -> {
        val message = if (error.message != null) "${executionContext.failureTitle}: ${error.message}" else executionContext.failureTitle
        val displayMessage = "$message\nSee idea.log for more details.\nIf you think this error should not appear, please report a bug."
        Pair(message, displayMessage)
      }
    }

    LOGGER.warn(message, error)

    when (failureHandlingMode) {
      FailureHandlingMode.AS_NOTIFICATION -> NotificationUtils.notifyError(executionContext.failureTitle,
                                                                           displayMessage,
                                                                           executionContext.project)

      FailureHandlingMode.AS_DIALOG -> Messages.showErrorDialog(executionContext.project,
                                                                executionContext.failureTitle,
                                                                displayMessage)
    }
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  @Suppress("UnstableApiUsage")
  private class BackgroundTask(canBeCancelled: Boolean,
                               private val parentAction: GradleUtilityAction<*>,
                               private val executionContext: ExecutionContext,
                               performInBackgroundOption: PerformInBackgroundOption)
    : Task.Backgroundable(executionContext.project, executionContext.title, canBeCancelled, performInBackgroundOption) {

    override fun run(progressIndicator: ProgressIndicator) {
      parentAction.runAction(executionContext, progressIndicator)
    }

    override fun onCancel() {
      parentAction.onCanceled0(executionContext)
    }

    override fun onThrowable(error: Throwable) {
      parentAction.onThrowable0(error, executionContext)
    }

    override fun onFinished() {
      parentAction.onFinished0(executionContext)
    }

    override fun onSuccess() {
      parentAction.onSuccess0(executionContext)
    }

    override fun whereToRunCallbacks(): EdtReplacementThread = EdtReplacementThread.EDT
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  private class ModalTask(canBeCancelled: Boolean,
                          private val parentAction: GradleUtilityAction<*>,
                          private val executionContext: ExecutionContext,
                          performInBackgroundOption: PerformInBackgroundOption)
    : Task.ConditionalModal(executionContext.project, executionContext.title, canBeCancelled, performInBackgroundOption) {

    override fun run(progressIndicator: ProgressIndicator) {
      parentAction.runAction(executionContext, progressIndicator)
    }

    override fun onCancel() {
      parentAction.onCanceled0(executionContext)
    }

    override fun onThrowable(error: Throwable) {
      parentAction.onThrowable0(error, executionContext)
    }

    override fun onFinished() {
      parentAction.onFinished0(executionContext)
    }

    override fun onSuccess() {
      parentAction.onSuccess0(executionContext)
    }

    override fun whereToRunCallbacks(): EdtReplacementThread = EdtReplacementThread.EDT
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  enum class FailureHandlingMode { AS_DIALOG, AS_NOTIFICATION }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  enum class ExecutionMode { BACKGROUND, MODAL, DIRECT }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  class ExecutionContext(val dataContext: DataContext,
                         val project: Project? = null,
                         val title: String,
                         val failureTitle: String)
}

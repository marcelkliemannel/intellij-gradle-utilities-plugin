package dev.turingcomplete.intellijgradleutilitiesplugin.common

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ide.actions.RevealFileAction
import com.intellij.idea.ActionsBundle
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.SystemInfo
import java.nio.file.Path
import kotlin.io.path.exists

open class OpenDirectoryAction
  : GradleUtilityAction<List<Path>>(createTitle(), null, AllIcons.Actions.MenuOpen,
                                    executionMode = ExecutionMode.DIRECT) {

  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private fun createTitle(): String {
      return if (SystemInfo.isMac) {
        ActionsBundle.message("action.RevealIn.name.mac")
      }
      else {
        ActionsBundle.message("action.RevealIn.name.other", RevealFileAction.getFileManagerName())
      }
    }
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    showOpensDialogIndicatorOnButtonText = false

    isVisible = { e -> directory(e.dataContext) != null }

    isEnabled = { e -> directory(e.dataContext)?.exists() ?: false }
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun runAction(executionContext: ExecutionContext, progressIndicator: ProgressIndicator) {
    directory(executionContext.dataContext)?.takeIf { it.exists() }?.let { BrowserUtil.browse(it) }
  }

  open fun directory(dataContext: DataContext): Path? = CommonDataKeys.SELECTED_DIRECTORY.getData(dataContext)?.path

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
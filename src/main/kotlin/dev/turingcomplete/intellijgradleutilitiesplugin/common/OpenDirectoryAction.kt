package dev.turingcomplete.intellijgradleutilitiesplugin.common

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ide.actions.RevealFileAction
import com.intellij.idea.ActionsBundle
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.SystemInfo
import java.nio.file.Path

class OpenDirectoryAction
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

    isVisible = { e -> CommonDataKeys.SELECTED_DIRECTORY.getData(e.dataContext) != null }
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun runAction(executionContext: ExecutionContext, progressIndicator: ProgressIndicator) {
    val selectedDirectory = CommonDataKeys.SELECTED_DIRECTORY.getData(executionContext.dataContext) ?: return
    BrowserUtil.browse(selectedDirectory.path)
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
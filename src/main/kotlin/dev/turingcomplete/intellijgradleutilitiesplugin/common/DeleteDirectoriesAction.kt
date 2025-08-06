package dev.turingcomplete.intellijgradleutilitiesplugin.common

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessProvider
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.NlsActions
import com.intellij.util.io.delete
import dev.turingcomplete.intellijgradleutilitiesplugin.other.safeCastTo
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor

open class DeleteDirectoriesAction(
  overrideTitle: @NlsActions.ActionText String? = null,
  description: @NlsActions.ActionDescription String? = null,
) : GradleUtilityAction<Void>(overrideTitle ?: "", description, AllIcons.Actions.GC) {

  // -- Companion Object ---------------------------------------------------- //

  companion object {
    private val LOG = Logger.getInstance(DeleteDirectoriesAction::class.java)
  }

  // -- Properties ---------------------------------------------------------- //
  // -- Initialization ------------------------------------------------------ //

  init {
    if (overrideTitle == null) {
      this.title = { _, dataContext ->
        val selectedDirectories = directories(dataContext)
        val numOfDirectories = selectedDirectories.size
        if (numOfDirectories == 1) "Delete Directory" else "Delete $numOfDirectories Directories"
      }
    }

    isVisible = { dataContext -> directories(dataContext).isNotEmpty() }
  }

  // -- Exported Methods ---------------------------------------------------- //

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  open fun directories(dataContext: DataContext): List<Directory> {
    return CommonDataKeys.SELECTED_DIRECTORIES.getData(dataContext)?.safeCastTo<List<Directory>>()
      ?: listOf()
  }

  final override fun runAction(
    executionContext: ExecutionContext,
    progressIndicator: ProgressIndicator,
  ) {
    val directories = directories(executionContext.dataContext)
    directories
      .asSequence()
      .map { it.path }
      .forEach { directory ->
        if (!Files.exists(directory)) {
          LOG.info("Directory '$directory' not deleted because it does not exists.")
          return
        }

        val statusMessage = "Deleting directory: $directory..."
        LOG.info(statusMessage)
        progressIndicator.text = statusMessage

        NonProjectFileWritingAccessProvider.disableChecksDuring {
          Files.walkFileTree(
            directory,
            object : SimpleFileVisitor<Path>() {
              override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                progressIndicator.checkCanceled()
                progressIndicator.text2 = directory.relativize(dir).toString()
                dir.delete(true)
                return FileVisitResult.CONTINUE
              }
            },
          )
        }
      }
  }

  // -- Private Methods ----------------------------------------------------- //
  // -- Inner Type ---------------------------------------------------------- //
}

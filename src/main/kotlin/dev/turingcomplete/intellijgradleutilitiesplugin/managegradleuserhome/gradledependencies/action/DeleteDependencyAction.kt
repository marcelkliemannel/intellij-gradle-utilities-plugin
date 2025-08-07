package dev.turingcomplete.intellijgradleutilitiesplugin.managegradleuserhome.gradledependencies.action

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessProvider
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.NlsActions
import com.intellij.util.io.delete
import dev.turingcomplete.intellijgradleutilitiesplugin.common.CommonDataKeys
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityAction
import dev.turingcomplete.intellijgradleutilitiesplugin.managegradleuserhome.gradledependencies.GradleDep
import dev.turingcomplete.intellijgradleutilitiesplugin.other.safeCastTo
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor

open class DeleteDependencyAction(overrideTitle: @NlsActions.ActionText String? = null,
                                  description: @NlsActions.ActionDescription String? = null)
  : GradleUtilityAction<Void>(overrideTitle ?: "", description, AllIcons.Actions.GC) {

  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private val LOG = Logger.getInstance(DeleteDependencyAction::class.java)
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    if (overrideTitle == null) {
      this.title = { _, e ->
        val selectedDirectories = directories(e.dataContext)
        val numOfDirectories = selectedDirectories.size
        if (numOfDirectories == 1) "Delete Dependency" else "Delete $numOfDirectories Dependencies"
      }
    }

    isVisible = { e -> directories(e.dataContext).isNotEmpty() }
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  open fun directories(dataContext: DataContext): List<GradleDep> {
    return CommonDataKeys.SELECTED_DIRECTORIES.getData(dataContext)?.safeCastTo<List<GradleDep>>() ?: listOf()
  }

  final override fun runAction(executionContext: ExecutionContext, progressIndicator: ProgressIndicator) {
    val directories = directories(executionContext.dataContext)
    directories.asSequence().flatMap { it.dirs.map { it.path } }.forEach { directory ->
      if (!Files.exists(directory)) {
        LOG.info("Directory '$directory' not deleted because it does not exists.")
        return
      }

      val statusMessage = "Deleting directory: $directory..."
      LOG.info(statusMessage)
      progressIndicator.text = statusMessage

      NonProjectFileWritingAccessProvider.disableChecksDuring {
        Files.walkFileTree(directory, object : SimpleFileVisitor<Path>() {
          override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
            progressIndicator.checkCanceled()
            progressIndicator.text2 = directory.relativize(dir).toString()
            dir.delete(true)
            return FileVisitResult.CONTINUE
          }
        })
      }
    }
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
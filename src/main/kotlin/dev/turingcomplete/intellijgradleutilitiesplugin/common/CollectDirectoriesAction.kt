package dev.turingcomplete.intellijgradleutilitiesplugin.common

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.NlsActions
import com.intellij.util.io.isDirectory
import org.apache.commons.io.FileUtils
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

abstract class CollectDirectoriesAction(title: @NlsActions.ActionText String,
                                        private val progressText: String,
                                        description: @NlsActions.ActionDescription String? = null)
  : GradleUtilityAction<List<Directory>>(title, description, AllIcons.Actions.Refresh, executionMode = ExecutionMode.MODAL) {

  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    val CALCULATE_SIZE: DataKey<Boolean> = DataKey.create("Gradle.Utilities.Plugin.CollectDirectoriesAction.CalculateSize")
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  abstract fun parentDirectory(): Path

  open fun filter(path: Path): Boolean = true

  override fun runAction(executionContext: ExecutionContext, progressIndicator: ProgressIndicator) {
    if (!Files.exists(parentDirectory())) {
      result(listOf())
      return
    }

    progressIndicator.text = progressText
    val calculateSize = CALCULATE_SIZE.getData(executionContext.dataContext) ?: false
    result(Files.list(parentDirectory()).use { path ->
      path.filter { it.isDirectory() && filter(it) }
              .map {
                val size = if (calculateSize) {
                  progressIndicator.text2 = "Calculating size of: ${it.fileName}..."
                  val sizeResult = FileUtils.sizeOfDirectory(it.toFile())
                  progressIndicator.text2 = ""
                  sizeResult
                }
                else {
                  null
                }
                Directory(it, size)
              }
              .toList()
    })
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
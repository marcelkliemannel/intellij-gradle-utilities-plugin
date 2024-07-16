package dev.turingcomplete.intellijgradleutilitiesplugin.managegradleuserhome.gradledependencies

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.progress.ProgressIndicator
import dev.turingcomplete.intellijgradleutilitiesplugin.common.CollectDirectoriesAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.Directory
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtils
import org.apache.commons.io.FileUtils
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

class CollectGradleDependenciesAction
  : GradleUtilityAction<List<GradleDep>>("Collect Gradle Distributions",
                        "Collects all downloaded Gradle distributions",
                        AllIcons.Actions.Refresh,
                        executionMode = ExecutionMode.MODAL,
      ) {

  // -- Companion Object -------------------------------------------------------------------------------------------- //
  companion object {
      val PROGRESS_TEXT = "Collecting Gradle daemon distributions..."
      val CALCULATE_SIZE: DataKey<Boolean> = DataKey.create("Gradle.Utilities.Plugin.CollectDirectoriesAction.CalculateSize")

  }
  // -- Properties -------------------------------------------------------------------------------------------------- //

  var searchFilter = ""

  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    showOpensDialogIndicatorOnButtonText = false
  }

    // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun runAction(executionContext: ExecutionContext, progressIndicator: ProgressIndicator) {
      val dir = GradleUtils.gradleUserHome().resolve(GradleUtils.MODULES)

      if (!Files.exists(dir)) {
          result(listOf())
          return
      }

      progressIndicator.text = PROGRESS_TEXT
      val calculateSize = CollectDirectoriesAction.CALCULATE_SIZE.getData(executionContext.dataContext) ?: false
      val deps = mutableMapOf<String, MutableMap<String, MutableMap<String, MutableList<Path>>>>()
      dir.listDirectoryEntries().asSequence().mapNotNull {
          if (it.name.startsWith("files-")) {
              it
          } else if (it.name.startsWith("metadata-")) {
              if (it.resolve("descriptors").exists()) {
                  it.resolve("descriptors")
              } else {
                  null
              }
          } else {
              null
          }
      }
      // packages
      .flatMap { root ->
          root.listDirectoryEntries().filter { it.isDirectory() }
      }
      // names
      .flatMap { pkg ->
          pkg.listDirectoryEntries().filter { it.isDirectory() }
      }
      // versions
      .flatMap { name ->
          name.listDirectoryEntries().filter { it.isDirectory() }
      }.forEach {
          deps.getOrPut(it.parent.parent.name) { mutableMapOf() }
              .getOrPut(it.parent.name) { mutableMapOf() }
              .getOrPut(it.name) { mutableListOf() }
              .add(it)
      }

        result(deps.flatMap { (group, groupDeps) ->
            groupDeps.flatMap { (name, nameDeps) ->
                nameDeps.map { (version, versionDeps) ->
                    GradleDep(group, name, version, versionDeps.map {
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
                    })
                }
            }
        }.filter {
            it.searchName.contains(searchFilter)
        })
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
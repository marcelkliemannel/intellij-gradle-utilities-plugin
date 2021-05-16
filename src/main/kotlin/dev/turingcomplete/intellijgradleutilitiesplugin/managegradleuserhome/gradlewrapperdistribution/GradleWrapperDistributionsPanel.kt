package dev.turingcomplete.intellijgradleutilitiesplugin.managegradleuserhome.gradlewrapperdistribution

import dev.turingcomplete.intellijgradleutilitiesplugin.common.Directory
import dev.turingcomplete.intellijgradleutilitiesplugin.managegradleuserhome.ManageGradleUserHomeDirectoriesPanel
import java.nio.file.Path

class GradleWrapperDistributionsPanel
  : ManageGradleUserHomeDirectoriesPanel(COLUMNS,
                                         CollectWrapperGradleDistributionsAction(),
                                         "Collecting Gradle wrapper distributions...",
                                         "No Gradle wrapper distributions") {

  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private val DISTRIBUTION_DIR_REGEX = Regex("^gradle-(?<version>.*?)-(?<type>all|bin)$")

    private val COLUMNS = listOf<Column<Directory>>(Column("Version") { parseVersion(it.path) },
                                                    Column("Type") { parseType(it.path) })

    private fun parseVersion(it: Path): String? {
      return DISTRIBUTION_DIR_REGEX.matchEntire(it.fileName.toString())?.groups?.get("version")?.value
    }

    private fun parseType(it: Path): String? {
      return DISTRIBUTION_DIR_REGEX.matchEntire(it.fileName.toString())?.groups?.get("type")?.value
    }
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //
  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
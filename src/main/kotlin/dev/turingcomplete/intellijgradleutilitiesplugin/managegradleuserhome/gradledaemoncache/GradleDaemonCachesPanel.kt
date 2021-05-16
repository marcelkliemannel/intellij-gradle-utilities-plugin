package dev.turingcomplete.intellijgradleutilitiesplugin.managegradleuserhome.gradledaemoncache

import dev.turingcomplete.intellijgradleutilitiesplugin.common.Directory
import dev.turingcomplete.intellijgradleutilitiesplugin.managegradleuserhome.ManageGradleUserHomeDirectoriesPanel

class GradleDaemonCachesPanel
  : ManageGradleUserHomeDirectoriesPanel(COLUMNS,
                                         CollectGradleDaemonCachesAction(),
                                         "Collecting Gradle daemon caches...",
                                         "No Gradle daemon caches") {

  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private val COLUMNS = listOf<Column<Directory>>(Column("Version") { it.path.fileName.toString() })
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //
  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
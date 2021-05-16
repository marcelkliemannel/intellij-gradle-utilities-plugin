package dev.turingcomplete.intellijgradleutilitiesplugin.managegradleuserhome.gradledaemoncache

import dev.turingcomplete.intellijgradleutilitiesplugin.common.CollectDirectoriesAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtils
import java.nio.file.Path

class CollectGradleDaemonCachesAction
  : CollectDirectoriesAction("Collect Gradle Daemon Caches",
                             "Collecting Gradle daemon caches...",
                             "Collects all Gradle daemon caches") {

  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private val DAEMON_CACHE_DIR_NAME_REGEX = Regex("^(\\d+\\.?)+$")

    private val DIST_DIR = Path.of("caches")
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    showOpensDialogIndicatorOnButtonText = false
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun parentDirectory(): Path = GradleUtils.gradleUserHome().resolve(DIST_DIR)

  override fun filter(path: Path): Boolean = DAEMON_CACHE_DIR_NAME_REGEX.matches(path.fileName.toString())

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
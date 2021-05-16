package dev.turingcomplete.intellijgradleutilitiesplugin.runninggradledaemon

import com.intellij.execution.process.ProcessInfo

data class GradleDaemon(val processInfo: ProcessInfo, val status: String?) {
  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private val VERSION_GRADLE_LAUNCHER_JAR_REGEX = Regex("gradle-launcher-(?<version>.*).jar")
  }

  // -- Variables --------------------------------------------------------------------------------------------------- //

  val version : String? = parseVersion()

  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exported Methods -------------------------------------------------------------------------------------------- //
  // -- Private Methods --------------------------------------------------------------------------------------------- //

  private fun parseVersion(): String? {
    return VERSION_GRADLE_LAUNCHER_JAR_REGEX.find(processInfo.commandLine)?.groupValues?.get(1)
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
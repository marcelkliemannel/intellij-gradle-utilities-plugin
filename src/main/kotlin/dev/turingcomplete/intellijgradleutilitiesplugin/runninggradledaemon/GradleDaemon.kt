package dev.turingcomplete.intellijgradleutilitiesplugin.runninggradledaemon

import com.intellij.execution.process.ProcessInfo
import java.time.Instant

class GradleDaemon(processInfo: ProcessInfo,
                   processHandle: ProcessHandle?,
                   val status: String?) {
  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private val VERSION_GRADLE_LAUNCHER_JAR_REGEX = Regex("gradle-launcher-(?<version>.*).jar")
  }

  // -- Variables --------------------------------------------------------------------------------------------------- //

  val pid = processInfo.pid.toLong()
  val commandLine = processInfo.commandLine
  val uptimeMillis: Long? = processHandle?.info()?.startInstant()?.map {
    Instant.now().toEpochMilli() - it.toEpochMilli()
  }?.orElse(null)

  val version: String? = parseVersion()

  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exported Methods -------------------------------------------------------------------------------------------- //
  // -- Private Methods --------------------------------------------------------------------------------------------- //

  private fun parseVersion(): String? {
    return VERSION_GRADLE_LAUNCHER_JAR_REGEX.find(commandLine)?.groupValues?.get(1)
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
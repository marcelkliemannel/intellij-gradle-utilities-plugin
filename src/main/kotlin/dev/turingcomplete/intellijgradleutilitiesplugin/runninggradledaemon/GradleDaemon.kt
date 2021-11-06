package dev.turingcomplete.intellijgradleutilitiesplugin.runninggradledaemon

import com.intellij.util.containers.orNull
import java.time.Instant
import kotlin.streams.asSequence

class GradleDaemon(processInfo: ProcessHandle, val status: String?) {
  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private val VERSION_GRADLE_LAUNCHER_JAR_REGEX = Regex("gradle-launcher-(?<version>.*).jar")
  }

  // -- Variables --------------------------------------------------------------------------------------------------- //

  val pid = processInfo.pid()
  val commandLine : String
  val uptimeMillis: Long?
  val version: String?

  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    val info = processInfo.info()
    commandLine = info.commandLine().orElse("")
    uptimeMillis = info.startInstant()?.map { Instant.now().toEpochMilli() - it.toEpochMilli() }?.orNull()
    version = VERSION_GRADLE_LAUNCHER_JAR_REGEX.find(commandLine)?.groupValues?.get(1)
  }

  // -- Exported Methods -------------------------------------------------------------------------------------------- //
  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
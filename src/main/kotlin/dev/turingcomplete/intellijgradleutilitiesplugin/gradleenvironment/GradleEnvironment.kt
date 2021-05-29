package dev.turingcomplete.intellijgradleutilitiesplugin.gradleenvironment

import java.nio.file.Path

class GradleEnvironment(val gradleWrapper: GradleWrapper?,
                        val gradleUserHomeDir: Path,
                        val systemGradle: SystemGradle?,
                        val projectProperties: List<Pair<String, String>>,
                        val userProperties: List<Pair<String, String>>,
                        val errors: List<String>) {

  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private val ENVIRONMENT_VARIABLES = listOf("GRADLE_OPTS", "GRADLE_USER_HOME", "JAVA_HOME")
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //

  val environmentVariables = ENVIRONMENT_VARIABLES.map { Pair(it, System.getProperty(it, null)) }

  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //
  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  class GradleWrapper(val version: String?,
                      val checksum: String?,
                      val wrapperProperties: List<Pair<String, String>>,
                      val checksumVerificationConfigured: Boolean)

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  class SystemGradle(val gradleHomeDir: Path, val version: String?)
}

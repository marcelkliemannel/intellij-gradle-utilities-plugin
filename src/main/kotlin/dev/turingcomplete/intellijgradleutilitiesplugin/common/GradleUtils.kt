package dev.turingcomplete.intellijgradleutilitiesplugin.common

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.nullize
import org.gradle.initialization.BuildLayoutParameters
import org.jetbrains.plugins.gradle.service.GradleInstallationManager
import org.jetbrains.plugins.gradle.util.GradleUtil
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

object GradleUtils {
  // -- Properties -------------------------------------------------------------------------------------------------- //

  private val SYSTEM_GRADLE_EXECUTABLE_FILE_NAME = "gradle${if (SystemInfo.isWindows) ".exe" else ""}"

  val DISTRIBUTIONS_DIR: Path = Path.of("wrapper", "dists")
  val MODULES: Path = Path.of("caches", "modules-2")

  private val GRADLE_VERSION_OUTPUT_REGEX = Regex("^Gradle (?<version>.*)$")
  private val GRADLE_DAEMON_STATUS_REGEX = Regex("^\\s+(?<pid>\\d+)\\s+(?<status>\\w+)\\s?.*$")

  private val LOGGER = Logger.getInstance(GradleUtils::class.java)

  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  fun gradleHome(project: Project?): Path? {
    val latestUsedGradleHome = GradleUtil.getLastUsedGradleHome().nullize()
    if (latestUsedGradleHome != null) {
      return Path.of(latestUsedGradleHome)
    }

    val gradleInstallationManager = ApplicationManager.getApplication().getService(GradleInstallationManager::class.java)

    val getAutodetectedGradleHomeWithoutParameter = {
      val method = gradleInstallationManager::class.java.getMethod("getAutodetectedGradleHome")
      (method.invoke(gradleInstallationManager) as File?)?.toPath()
    }
    val getAutodetectedGradleHomeWithProjectParameter = {
      val method = gradleInstallationManager::class.java.getMethod("getAutodetectedGradleHome", Project::class.java)
      (method.invoke(gradleInstallationManager, project) as File?)?.toPath()
    }
    return findAutodetectGradleHome(ArrayDeque(listOf(getAutodetectedGradleHomeWithoutParameter,
                                                      getAutodetectedGradleHomeWithProjectParameter)))
  }

  fun gradleUserHome(): Path {
    return BuildLayoutParameters().gradleUserHomeDir.toPath()
  }

  fun findSystemGradleExecutable(project: Project?): Path? {
    val systemGradleExecutableFullPath = gradleHome(project)?.resolve(Path.of("bin", SYSTEM_GRADLE_EXECUTABLE_FILE_NAME))
    return if (systemGradleExecutableFullPath != null && Files.exists(systemGradleExecutableFullPath)) {
      systemGradleExecutableFullPath
    }
    else {
      null
    }
  }

  fun findGradlewExecutable(projectDir: VirtualFile): VirtualFile? {
    val gradlewExtension = if (SystemInfo.isWindows) ".bat" else ""
    return projectDir.findChild("gradlew$gradlewExtension")
  }

  fun findGradleWrapperProperties(projectDir: VirtualFile): VirtualFile? {
    return projectDir.findChild("gradle")?.findChild("wrapper")?.findChild("gradle-wrapper.properties")
  }

  fun determineGradleVersion(workingDir: Path?, gradleExecutable: Path): String? {
    var process: Process? = null
    try {
      process = ProcessBuilder().apply {
        command(listOf(gradleExecutable.toString(), "-q", "--version", "--no-daemon", "--console=plain"))
        workingDir?.let { directory(it.toFile()) }
        redirectErrorStream(true)
      }.start()

      val gradleVersion = process.inputStream.bufferedReader().use { it.readText() }.lineSequence()
              .mapNotNull { GRADLE_VERSION_OUTPUT_REGEX.find(it)?.groups?.get("version")?.value }
              .firstOrNull()
      process.waitFor()

      return gradleVersion
    }
    catch (e: Throwable) {
      process?.destroy()
      LOGGER.warn("Failed to determine Gradle version.", e)
      return null
    }
  }

  fun determineGradleDaemonStatus(workingDir: Path?, gradleExecutable: Path): Map<Long, String> {
    var process: Process? = null
    try {
      process = ProcessBuilder().apply {
        command(listOf(gradleExecutable.toString(), "--status", "--no-daemon", "--console=plain"))
        workingDir?.let { directory(it.toFile()) }
        redirectErrorStream(true)
      }.start()

      val gradleDaemonStatus = process.inputStream.bufferedReader()
              .use { it.readText() }
              .lineSequence()
              .mapNotNull {
                GRADLE_DAEMON_STATUS_REGEX.find(it)?.groups?.let { groups ->
                  groups["pid"]!!.value.toLong() to groups["status"]!!.value
                }
              }
              .toMap()
      process.waitFor()

      return gradleDaemonStatus
    }
    catch (e: Throwable) {
      process?.destroy()
      LOGGER.warn("Failed to determine Gradle version.", e)
      return mapOf()
    }
  }

  fun isChecksumVerificationConfigured(gradleWrapperProperties: List<Pair<String, String>>) : Boolean {
    return gradleWrapperProperties.any { it.first == "distributionSha256Sum" }
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //

  /**
   * Workaround for API breaking change in
   * [Gradle plugin](https://github.com/JetBrains/intellij-community/commit/ed763b8408cdd1dbba0125254ef4cd3ec754a1e5)
   */
  private fun findAutodetectGradleHome(methodsToCheck: Queue<() -> Path?>): Path? {
    val methodToCheck = methodsToCheck.poll()
    if (methodToCheck == null) {
      LOGGER.warn("Failed to call 'org.jetbrains.plugins.gradle.service.GradleInstallationManager#autodetectedGradleHome'. " +
                  "Please report this as a bug of the Gradle Utilises plugin and include the following list: " +
                  GradleInstallationManager::class.java.methods.joinToString(", ") { "${it.name}(${it.parameterCount})" })
      return null
    }

    try {
      return methodToCheck()
    }
    catch (e: SecurityException) {
      return findAutodetectGradleHome(methodsToCheck)
    }
    catch (e: NoSuchMethodException) {
      return findAutodetectGradleHome(methodsToCheck)
    }
    catch (e: IllegalAccessException) {
      return findAutodetectGradleHome(methodsToCheck)
    }
    catch (e: IllegalArgumentException) {
      return findAutodetectGradleHome(methodsToCheck)
    }
    catch (e: InvocationTargetException) {
      return findAutodetectGradleHome(methodsToCheck)
    }
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
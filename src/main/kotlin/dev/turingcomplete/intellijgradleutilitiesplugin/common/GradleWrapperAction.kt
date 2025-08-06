package dev.turingcomplete.intellijgradleutilitiesplugin.common

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.NlsActions
import com.intellij.openapi.util.PropertiesUtil
import com.intellij.openapi.vfs.VirtualFile
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.Icon

abstract class GradleWrapperAction<R>(
  title: @NlsActions.ActionText String,
  description: @NlsActions.ActionDescription String? = null,
  icon: Icon? = null,
) : GradleUtilityAction<R>(title, description, icon, executionMode = ExecutionMode.MODAL) {

  // -- Companion Object ---------------------------------------------------- //
  // -- Properties ---------------------------------------------------------- //
  // -- Initialization ------------------------------------------------------ //
  // -- Exported Methods ---------------------------------------------------- //

  protected fun findGradlewExecutable(
    projectDir: VirtualFile,
    progressIndicator: ProgressIndicator,
  ): VirtualFile? {
    progressIndicator.checkCanceled()
    return GradleUtils.findGradlewExecutable(projectDir)
  }

  protected fun determineGradleWrapperVersion(
    projectDir: VirtualFile,
    gradlewExecutable: VirtualFile,
    progressIndicator: ProgressIndicator,
  ): String? {
    progressIndicator.checkCanceled()
    return GradleUtils.determineGradleVersion(projectDir.toNioPath(), gradlewExecutable.toNioPath())
  }

  protected fun findGradleWrapperJar(
    projectDir: VirtualFile,
    progressIndicator: ProgressIndicator,
  ): VirtualFile? {
    progressIndicator.checkCanceled()
    return projectDir.findFileByRelativePath("gradle/wrapper/gradle-wrapper.jar")
  }

  protected fun readGradleWrapperProperties(
    projectDir: VirtualFile,
    progressIndicator: ProgressIndicator,
    handleError: (String, Exception) -> Unit,
  ): List<Pair<String, String>> {

    return GradleUtils.findGradleWrapperProperties(projectDir)?.let { gradleWrapperProperties ->
      readProperties(
        "Reading Gradle wrapper properties...",
        gradleWrapperProperties.toNioPath(),
        progressIndicator,
        handleError,
      )
    } ?: listOf()
  }

  // -- Private Methods ----------------------------------------------------- //

  protected fun readProperties(
    progressText: String,
    propertiesFile: Path,
    progressIndicator: ProgressIndicator,
    handleError: (String, Exception) -> Unit,
  ): List<Pair<String, String>> {

    progressIndicator.checkCanceled()

    try {
      progressIndicator.text = progressText

      if (!Files.exists(propertiesFile)) {
        return listOf()
      }

      return FileReader(propertiesFile.toFile()).use {
        PropertiesUtil.loadProperties(it)
          .map { property -> Pair(property.key, property.value) }
          .sortedBy { it.first }
      }
    } catch (e: Exception) {
      handleError("Failed to read properties file: $propertiesFile", e)
      return listOf()
    }
  }

  // -- Inner Type ---------------------------------------------------------- //
}

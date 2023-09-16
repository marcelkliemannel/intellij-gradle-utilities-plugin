package dev.turingcomplete.intellijgradleutilitiesplugin.gradleenvironment

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.ScrollPaneFactory
import com.intellij.util.ui.JBUI
import dev.turingcomplete.intellijgradleutilitiesplugin.common.DeleteDirectoriesAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityActionFailedException
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtils
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleWrapperAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.GradleUtilityDialog
import org.apache.commons.codec.digest.DigestUtils
import java.awt.Dimension
import java.io.FileInputStream
import javax.swing.ScrollPaneConstants

class CollectGradleEnvironmentInformationAction
  : GradleWrapperAction<GradleEnvironment>("Collect Gradle Environment Information",
                                           "Collects information about the current Gradle environment.") {

  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private val LOG = Logger.getInstance(DeleteDirectoriesAction::class.java)
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun runAction(executionContext: ExecutionContext, progressIndicator: ProgressIndicator) {
    val project = CommonDataKeys.PROJECT.getData(executionContext.dataContext) ?: return
    val projectDir = project.guessProjectDir() ?: throw GradleUtilityActionFailedException("Failed to determine project directory.")

    val errors = mutableListOf<String>()
    val handleError: (String, Exception) -> Unit = { errorMessage, e ->
      LOG.warn(errorMessage, e)
      errors.add("$errorMessage${if (e.message != null) "\n${e.message}" else ""}")
    }

    val gradleSystem = collectSystemGradleInformation(executionContext.project, progressIndicator, handleError)
    val gradleWrapper = collectGradleWrapperInformation(projectDir, progressIndicator, handleError)
    val gradleUserHomeDir = GradleUtils.gradleUserHome()
    val projectProperties = readProperties("Reading project properties...", projectDir.toNioPath().resolve("gradle.properties"), progressIndicator, handleError)
    val userProperties = readProperties("Reading user properties...", gradleUserHomeDir.resolve("gradle.properties"), progressIndicator, handleError)

    result(GradleEnvironment(gradleWrapper, gradleUserHomeDir, gradleSystem, projectProperties, userProperties, errors))
  }

  override fun onSuccess(result: GradleEnvironment?, executionContext: ExecutionContext) {
    result as GradleEnvironment

    ApplicationManager.getApplication().invokeLater {
      GradleUtilityDialog.show("Gradle Environment", {
        ScrollPaneFactory.createScrollPane(GradleEnvironmentPanel(result),
                                           ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                           ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER).apply {
          border = JBUI.Borders.empty()
          viewportBorder = JBUI.Borders.empty()
        }
      }, Dimension(650, 500), executionContext.project)
    }
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //

  private fun collectSystemGradleInformation(project: Project?,
                                             progressIndicator: ProgressIndicator,
                                             handleError: (String, Exception) -> Unit): GradleEnvironment.SystemGradle? {
    progressIndicator.checkCanceled()

    try {
      progressIndicator.text = "Determine system Gradle version..."

      val gradleHomeDir = GradleUtils.gradleHome(project) ?: return null

      val systemGradleVersion: String? = GradleUtils.findSystemGradleExecutable(project)?.let { systemGradleExecutable ->
        GradleUtils.determineGradleVersion(null, systemGradleExecutable)
      }

      return GradleEnvironment.SystemGradle(gradleHomeDir, systemGradleVersion)
    }
    catch (e: Exception) {
      handleError("Failed to determine system Gradle version.", e)
      return null
    }
  }

  private fun collectGradleWrapperInformation(projectDir: VirtualFile,
                                              progressIndicator: ProgressIndicator,
                                              handleError: (String, Exception) -> Unit): GradleEnvironment.GradleWrapper? {
    progressIndicator.checkCanceled()

    try {
      progressIndicator.text = "Finding Gradle wrapper JAR..."
      val gradleWrapperJarChecksum = findGradleWrapperJar(projectDir, progressIndicator)?.let { gradleWrapperJar ->
        calculateGradleWrapperJarChecksum(progressIndicator, gradleWrapperJar)
      }

      progressIndicator.text = "Finding Gradle wrapper executable..."
      val gradleWrapperVersion = findGradlewExecutable(projectDir, progressIndicator)?.let { gradlewExecutable ->
        progressIndicator.text = "Determining Gradle wrapper version..."
        determineGradleWrapperVersion(projectDir, gradlewExecutable, progressIndicator)
      }

      val gradleWrapperProperties = readGradleWrapperProperties(projectDir, progressIndicator, handleError)

      val checksumVerificationConfigured = GradleUtils.isChecksumVerificationConfigured(gradleWrapperProperties)

      return GradleEnvironment.GradleWrapper(gradleWrapperVersion,
                                             gradleWrapperJarChecksum,
                                             gradleWrapperProperties,
                                             checksumVerificationConfigured)
    }
    catch (e: Exception) {
      handleError("Failed to collect Gradle wrapper information.", e)
      return null
    }
  }

  private fun calculateGradleWrapperJarChecksum(progressIndicator: ProgressIndicator, gradleWrapperJar: VirtualFile): String {
    progressIndicator.checkCanceled()
    progressIndicator.text = "Calculating Gradle wrapper JAR checksum..."
    return FileInputStream(gradleWrapperJar.toNioPath().toFile()).use { DigestUtils.sha256Hex(it) }
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
package dev.turingcomplete.intellijgradleutilitiesplugin.verification

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.guessProjectDir
import com.intellij.ui.ScrollPaneFactory
import com.intellij.util.ui.JBUI
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityActionFailedException
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtils
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleWrapperAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.GradleUtilityDialog
import org.apache.commons.codec.digest.DigestUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import java.awt.Dimension
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.ScrollPaneConstants
import kotlin.streams.asSequence


class VerifyGradleFilesAction
  : GradleWrapperAction<VerificationResult>("Verify Gradle wrapper JAR and distributions",
                                            "Verifies the integrity of the project Gradle wrapper JAR and the " +
                                            "downloaded Gradle distributions by matching the SHA-256 checksums with " +
                                            "the official ones.") {

  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private val LOG = Logger.getInstance(VerifyGradleFilesAction::class.java)

    private const val DISTRIBUTIONS_BASE_URL = "https://services.gradle.org/distributions/"
    private val DISTRIBUTION_ZIP_REGEX = Regex("^gradle-(?<version>.*?)-(?<type>all|bin)\\.zip$")
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    showOpensDialogIndicatorOnButtonText = false

    isVisible = { e -> CommonDataKeys.PROJECT.getData(e.dataContext) != null }
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun runAction(executionContext: ExecutionContext, progressIndicator: ProgressIndicator) {

    val errors = mutableListOf<String>()
    val handleError: (String, Exception) -> Unit = { errorMessage, e ->
      LOG.warn(errorMessage, e)
      errors.add("$errorMessage${if (e.message != null) "\n${e.message}" else ""}")
    }

    val gradleWrapperJar = verifyGradleWrapperJar(progressIndicator, executionContext, handleError)
    val gradleDistributions = verifyGradleDistributions(progressIndicator, handleError)
    val fileVerificationResults = sequenceOf(listOf(gradleWrapperJar), gradleDistributions)
            .flatten().filterNotNull().toList()
    result(VerificationResult(fileVerificationResults, errors))
  }

  private fun verifyGradleWrapperJar(progressIndicator: ProgressIndicator,
                                     executionContext: ExecutionContext,
                                     handleError: (String, Exception) -> Unit): VerificationResult.File? {

    progressIndicator.checkCanceled()

    try {
      progressIndicator.text = "Verifying Gradle wrapper JAR..."

      val projectDir = executionContext.project?.guessProjectDir() ?: return null

      progressIndicator.text2 = "Finding Gradle wrapper JAR..."
      val gradleWrapperJar = findGradleWrapperJar(projectDir, progressIndicator) ?: return null
      val actualChecksum = calculateChecksumOfGradleFile(progressIndicator, gradleWrapperJar.toNioPath())

      progressIndicator.text2 = "Finding Gradle wrapper executable..."
      val gradlewExecutable = findGradlewExecutable(projectDir, progressIndicator) ?: return null
      progressIndicator.text2 = "Determining Gradle wrapper version..."
      val gradleWrapperVersion = determineGradleWrapperVersion(projectDir, gradlewExecutable, progressIndicator)
                                 ?: throw GradleUtilityActionFailedException("Couldn't determine Gradle wrapper version.")

      val sha256FileName = "gradle-$gradleWrapperVersion-wrapper.jar.sha256"
      val expectedChecksum = requestChecksumForGradleFile(progressIndicator, sha256FileName)

      val warnings = mutableListOf<VerificationResult.Warning>()
      val readGradleWrapperProperties = readGradleWrapperProperties(projectDir, progressIndicator, handleError)
      if (!GradleUtils.isChecksumVerificationConfigured(readGradleWrapperProperties)) {
        warnings.add(VerificationResult.Warning("Checksum verification not configured.", "https://docs.gradle.org/current/userguide/gradle_wrapper.html#configuring_checksum_verification"))
      }

      return VerificationResult.File(gradleWrapperJar.toNioPath(), "Project Gradle Wrapper JAR",
                                     actualChecksum, expectedChecksum,
                                     "$DISTRIBUTIONS_BASE_URL$sha256FileName",
                                     "The SHA-256 checksum of the Gradle wrapper JAR matches the expected one for the version $gradleWrapperVersion.",
                                     "The SHA-256 checksum of the Gradle wrapper JAR does not match the expected one for the version $gradleWrapperVersion.",
                                     warnings)
    }
    catch (e: Exception) {
      handleError("Failed to verify Gradle wrapper JAR.", e)
      return null
    }
  }

  private fun verifyGradleDistributions(progressIndicator: ProgressIndicator,
                                        handleError: (String, Exception) -> Unit): List<VerificationResult.File> {

    val distributionsDir = GradleUtils.gradleUserHome().resolve(GradleUtils.DISTRIBUTIONS_DIR)
    if (!Files.exists(distributionsDir)) {
      return listOf()
    }

    return Files.walk(distributionsDir, 3).use { files ->
      files.filter(Files::isRegularFile)
              .filter { DISTRIBUTION_ZIP_REGEX.matches(it.fileName.toString()) }
              .asSequence()
              .mapNotNull { verifyGradleDistribution(it, progressIndicator, handleError) }
              .toList()
    }
  }

  private fun verifyGradleDistribution(distributionFile: Path,
                                       progressIndicator: ProgressIndicator,
                                       handleError: (String, Exception) -> Unit): VerificationResult.File? {

    progressIndicator.checkCanceled()

    try {
      progressIndicator.text = "Verifying Gradle distribution: ${distributionFile.fileName}..."

      val actualChecksum = calculateChecksumOfGradleFile(progressIndicator, distributionFile)

      val sha256FileName = "${distributionFile.fileName}.sha256"
      val expectedChecksum = requestChecksumForGradleFile(progressIndicator, sha256FileName)

      return VerificationResult.File(distributionFile, "Downloaded Gradle distribution file '${distributionFile.fileName}'",
                                     actualChecksum, expectedChecksum,
                                     "$DISTRIBUTIONS_BASE_URL$sha256FileName")
    }
    catch (e: Exception) {
      handleError("Failed to verify Gradle distribution: ${distributionFile.fileName}", e)
      return null
    }
  }

  override fun onSuccess(result: VerificationResult?, executionContext: ExecutionContext) {
    result as VerificationResult

    ApplicationManager.getApplication().invokeLater {
      GradleUtilityDialog.show("Gradle Wrapper JAR and Distributions Verification Result",
                               {
                                 ScrollPaneFactory.createScrollPane(VerificationResultPanel(result),
                                                                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER).apply {
                                   border = JBUI.Borders.empty()
                                   viewportBorder = JBUI.Borders.empty()
                                 }
                               },
                               Dimension(700, 500),
                               executionContext.project)
    }
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //

  private fun calculateChecksumOfGradleFile(progressIndicator: ProgressIndicator, gradleFile: Path): String {
    progressIndicator.checkCanceled()
    progressIndicator.text2 = "Calculating checksum of: ${gradleFile.fileName}..."
    return FileInputStream(gradleFile.toFile()).use { DigestUtils.sha256Hex(it) }
  }

  private fun requestChecksumForGradleFile(progressIndicator: ProgressIndicator, sha256FileName: String): String {
    progressIndicator.checkCanceled()

    progressIndicator.text2 = "Requesting SHA-256 checksum for $sha256FileName..."

    return HttpClients.createDefault().use { httpclient ->
      val sha256Get = HttpGet("$DISTRIBUTIONS_BASE_URL$sha256FileName")
      httpclient.execute(sha256Get).use { response ->
        if (response.statusLine.statusCode != 200) {
          throw GradleUtilityActionFailedException("Gradle website returned the unexpected status code ${response.statusLine.statusCode} " +
                                                   "for URL: ${sha256Get.uri}")
        }

        EntityUtils.toString(response.entity)
      }
    }
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
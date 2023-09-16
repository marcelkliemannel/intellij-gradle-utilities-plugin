package dev.turingcomplete.intellijgradleutilitiesplugin.latestgradlereleases

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.ui.ScrollPaneFactory
import com.intellij.util.net.ssl.CertificateManager
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityActionFailedException
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.GradleUtilityDialog
import org.apache.http.HttpEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils
import org.jetbrains.annotations.TestOnly
import java.awt.Dimension
import java.util.*

class FindLatestGradleReleasesAction
  : GradleUtilityAction<LatestGradleReleases>("Find the Latest Gradle Releases",
                                              "Finds the latest Gradle releases.") {

  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private val GIT_HUB_API_HEADER = BasicHeader("Accept", "application/vnd.github.v3+json")
    @TestOnly
    val GRADLE_RELEASE_NAME_VERSION_REGEX = Regex("^(?<major>\\d+)(?:.(?<minor>\\d+))?(?:.(?<patch>\\d+))?(?:\\s+(?<preRelease>.+))?\$")
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun runAction(executionContext: ExecutionContext, progressIndicator: ProgressIndicator) {
    val latestProductiveGradleReleases = LinkedHashMap<Int, GradleGitHubRelease>()
    val latestPreReleaseGradleReleases = LinkedHashMap<Int, GradleGitHubRelease>()
    val errors = mutableListOf<String>()

    progressIndicator.text = "Requesting latest Gradle releases from GitHub..."
    requestLatestGradleGitHubReleases().forEach { gitHubRelease ->
      val versionNameMatch = GRADLE_RELEASE_NAME_VERSION_REGEX.matchEntire(gitHubRelease.name)
      if (versionNameMatch != null) {
        val major = versionNameMatch.groups["major"]!!.value.toInt()
        if (gitHubRelease.preRelease) {
          latestPreReleaseGradleReleases.computeIfAbsent(major) { gitHubRelease }
        }
        else {
          latestProductiveGradleReleases.computeIfAbsent(major) { gitHubRelease }
        }
      }
      else {
        errors.add("Failed to parse Gradle GitHub release version: ${gitHubRelease.name}")
      }
    }

    result(LatestGradleReleases(latestProductiveGradleReleases.values.toList(),
                                latestPreReleaseGradleReleases.values.toList(),
                                errors))
  }

  /**
   * The returned list is ordered by the release date.
   */
  @TestOnly
  fun requestLatestGradleGitHubReleases(): List<GradleGitHubRelease> {
    HttpClients.custom().setSSLContext(CertificateManager.getInstance().sslContext).build().use { httpclient ->
      val releasesGet = HttpGet("https://api.github.com/repos/gradle/gradle/releases?per_page=100").apply {
        addHeader(GIT_HUB_API_HEADER)
      }

      httpclient.execute(releasesGet).use { response ->
        if (response.statusLine.statusCode != 200) {
          throw GradleUtilityActionFailedException("GitHub API returned the unexpected status code ${response.statusLine.statusCode} " +
                                                   "for URL: ${releasesGet.uri}. Please check your internet connection " +
                                                   "and if the error persist, please report this as a bug for the Gradle " +
                                                   "utilities plugin.")
        }

        val responseEntity: HttpEntity = response.entity
        val objectMapper = ObjectMapper()
                .registerKotlinModule()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val releases = objectMapper.readValue(responseEntity.content, Array<GradleGitHubRelease>::class.java)

        EntityUtils.consume(responseEntity)

        return releases.toList()
      }
    }
  }

  override fun onSuccess(result: LatestGradleReleases?, executionContext: ExecutionContext) {
    result as LatestGradleReleases

    ApplicationManager.getApplication().invokeLater {
      GradleUtilityDialog.show("Latest Gradle Releases",
                               { ScrollPaneFactory.createScrollPane(LatestGradleReleasesPanel(result), true) },
                               Dimension(720, 500),
                               executionContext.project)
    }
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
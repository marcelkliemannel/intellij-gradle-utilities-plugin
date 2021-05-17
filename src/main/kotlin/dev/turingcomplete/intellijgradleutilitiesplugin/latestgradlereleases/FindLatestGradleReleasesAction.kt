package dev.turingcomplete.intellijgradleutilitiesplugin.latestgradlereleases

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.ui.ScrollPaneFactory
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityActionFailedException
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleWrapperAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.GradleUtilityDialog
import org.apache.http.HttpEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils
import java.awt.Dimension
import java.util.*

class FindLatestGradleReleasesAction
  : GradleWrapperAction<Pair<List<GradleRelease>, List<GradleRelease>>>("Find the Latest Gradle Releases",
                                                                        "Finds the latest Gradle releases.") {

  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private val LOG = Logger.getInstance(FindLatestGradleReleasesAction::class.java)

    private val GIT_HUB_API_HEADER = BasicHeader("Accept", "application/vnd.github.v3+json")
    private val GRADLE_RELEASE_VERSION_REGEX = Regex("^(?<major>\\d+)(?:.(?<minor>\\d+))?(?:.(?<patch>\\d+))?(?:\\s+(?<preRelease>.+))?\$")
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun runAction(executionContext: ExecutionContext, progressIndicator: ProgressIndicator) {
    val latestProductiveGradleReleases = LinkedHashMap<Int, GradleRelease>()
    val latestPreReleaseGradleReleases = LinkedHashMap<Int, GradleRelease>()

    requestLatestGradleGitHubReleases(progressIndicator).forEach { gradleRelease ->
      val versionNameMatch = GRADLE_RELEASE_VERSION_REGEX.matchEntire(gradleRelease.name)
      if (versionNameMatch != null) {
        val major = versionNameMatch.groups["major"]!!.value.toInt()
        if (gradleRelease.preRelease) {
          latestPreReleaseGradleReleases.computeIfAbsent(major) {
            GradleRelease(gradleRelease.name, gradleRelease.publishedAt, gradleRelease.tagName)
          }
        }
        else {
          latestProductiveGradleReleases.computeIfAbsent(major) {
            GradleRelease(gradleRelease.name, gradleRelease.publishedAt, gradleRelease.tagName)
          }
        }
      }
      else {
        LOG.warn("Failed to parse Gradle GitHub release version: ${gradleRelease.name}. Please report this " +
                 "as a bug for the Gradle utilities plugin.")
      }
    }

    result(Pair(latestProductiveGradleReleases.values.toList(), latestPreReleaseGradleReleases.values.toList()))
  }

  override fun onSuccess(result: Pair<List<GradleRelease>, List<GradleRelease>>?, executionContext: ExecutionContext) {
    result as Pair<List<GradleRelease>, List<GradleRelease>>

    GradleUtilityDialog.show("Latest Gradle Releases",
                             { ScrollPaneFactory.createScrollPane(LatestGradleReleasesPanel(result.first, result.second), true) },
                             Dimension(720, 500),
                             executionContext.project)
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //

  private fun requestLatestGradleGitHubReleases(progressIndicator: ProgressIndicator): Array<GitHubRelease> {
    progressIndicator.text = "Requesting latest Gradle releases from GitHub..."

    HttpClients.createDefault().use { httpclient ->
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
        val releases = objectMapper.readValue(responseEntity.content, Array<GitHubRelease>::class.java)

        EntityUtils.consume(responseEntity)

        return releases
      }
    }
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  private class GitHubRelease(val name: String,

                              @JsonProperty("prerelease")
                              val preRelease: Boolean,

                              @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") // ISO 8601
                              @JsonProperty("published_at")
                              val publishedAt: Date,

                              @JsonProperty("tag_name")
                              val tagName: String)
}
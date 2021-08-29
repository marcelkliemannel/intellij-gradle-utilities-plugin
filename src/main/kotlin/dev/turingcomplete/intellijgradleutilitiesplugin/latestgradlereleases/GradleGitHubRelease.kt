package dev.turingcomplete.intellijgradleutilitiesplugin.latestgradlereleases

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class GradleGitHubRelease(val name: String,

                          @JsonProperty("prerelease")
                          val preRelease: Boolean,

                          @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'") // ISO 8601
                          @JsonProperty("published_at")
                          val publishedAt: Date,

                          @JsonProperty("tag_name")
                          val tagName: String) {

  // -- Companion Object -------------------------------------------------------------------------------------------- //
  // -- Properties -------------------------------------------------------------------------------------------------- //

  val gradleWrapperVersion: String by lazy { parseWrapperVersion() }

  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //
  // -- Private Methods --------------------------------------------------------------------------------------------- //

  /**
   * Rules:
   * 7.1.1 -> 7.1.1
   * 7.1 -> 7.1
   * 7.2 RC39 -> 7.2-rc-39
   * 7.0 M1 -> 7.0-milestone-1
   */
  private fun parseWrapperVersion(): String {
    return name.replace(" RC", "-rc-").replace(" M", "-milestone-")
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
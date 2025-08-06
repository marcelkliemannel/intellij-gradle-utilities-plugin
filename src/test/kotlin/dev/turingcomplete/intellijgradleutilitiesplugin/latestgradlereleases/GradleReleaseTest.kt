package dev.turingcomplete.intellijgradleutilitiesplugin.latestgradlereleases

import java.util.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class GradleReleaseTest {
  // -- Companion Object ---------------------------------------------------- //

  companion object {
    private lateinit var LATEST_GRADLE_RELEASES: List<GradleGitHubRelease>

    @BeforeAll
    @JvmStatic
    fun setUp() {
      LATEST_GRADLE_RELEASES = FindLatestGradleReleasesAction().requestLatestGradleGitHubReleases()
      Assertions.assertTrue(LATEST_GRADLE_RELEASES.size > 50)
    }
  }

  // -- Properties ---------------------------------------------------------- //
  // -- Initialization ------------------------------------------------------ //
  // -- Exported Methods ---------------------------------------------------- //

  @Test
  fun testGradleReleaseNameVersionRegex() {
    LATEST_GRADLE_RELEASES.forEach {
      Assertions.assertTrue(
        FindLatestGradleReleasesAction.GRADLE_RELEASE_NAME_VERSION_REGEX.matches(it.name),
        "Gradle GitHub release tag name '${it.name}' does not match regex.",
      )
    }
  }

  @Test
  fun testParsingOfGradleWrapperVersion() {
    LATEST_GRADLE_RELEASES.forEach {
      Assertions.assertNotNull(
        it.gradleWrapperVersion,
        "Gradle GitHub release tag name '${it.name}' does not match regex.",
      )
    }
  }

  @ParameterizedTest
  @CsvSource(
    "7.2 RC3|7.2-rc-3",
    "7.2 RC39|7.2-rc-39",
    "7.1.1|7.1.1",
    "7.0.2|7.0.2",
    "7.1|7.1",
    "7.0|7.0",
    "7.0 M1|7.0-milestone-1",
    delimiter = '|',
  )
  fun testGradleWrapperVersionParsing(releaseName: String, expectedWrapperVersion: String) {
    val actualWrapperVersion =
      GradleGitHubRelease(releaseName, false, Date(), "foo").gradleWrapperVersion
    Assertions.assertEquals(expectedWrapperVersion, actualWrapperVersion)
  }

  // -- Private Methods ----------------------------------------------------- //
  // -- Inner Type ---------------------------------------------------------- //
}

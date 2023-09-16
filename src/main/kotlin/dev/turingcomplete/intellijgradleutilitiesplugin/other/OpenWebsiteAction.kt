package dev.turingcomplete.intellijgradleutilitiesplugin.other

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.progress.ProgressIndicator
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityAction

abstract class OpenWebsiteAction(linkTitle: String, private val url: String)
  : GradleUtilityAction<Void>(linkTitle,
                              "Opens website '$linkTitle'",
                              executionMode = ExecutionMode.DIRECT) {

  // -- Companion Object -------------------------------------------------------------------------------------------- //
  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    showOpensDialogIndicatorOnButtonText = false
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun runAction(executionContext: ExecutionContext, progressIndicator: ProgressIndicator) {
    BrowserUtil.browse(url)
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  class GradleUserManual : OpenWebsiteAction("Gradle User Manual", "https://docs.gradle.org/current/userguide/userguide.html")
  class GradleUserManualCommandLineInterface : OpenWebsiteAction("User Manual: Command Line Interface", "https://docs.gradle.org/current/userguide/command_line_interface.html")
  class GradleUserManualGroovyDslReference : OpenWebsiteAction("User Manual: Groovy DSL Reference", "https://docs.gradle.org/current/dsl/index.html")

  class GradleReleaseNotes : OpenWebsiteAction("Gradle Release Notes", "https://docs.gradle.org/current/release-notes.html")
  class GradlePlugins : OpenWebsiteAction("Gradle Plugins", "https://plugins.gradle.org")
  class GradleForums : OpenWebsiteAction("Gradle Forums", "https://discuss.gradle.org")
  class GradleBlog : OpenWebsiteAction("Gradle Blog", "https://blog.gradle.org")

  class GitHubGradle : OpenWebsiteAction("GitHub: Gradle", "https://github.com/gradle/gradle")
}
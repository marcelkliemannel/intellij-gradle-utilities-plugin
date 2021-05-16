package dev.turingcomplete.intellijgradleutilitiesplugin.latestgradlereleases

import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.JBColor
import com.intellij.ui.border.CustomLineBorder
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.text.JBDateFormat
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.*
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.UiUtils
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.border.CompoundBorder

class LatestGradleReleasesPanel(productiveReleases: List<GradleRelease>,
                                preReleaseReleases: List<GradleRelease>) : JPanel(GridBagLayout()) {

  // -- Companion Object -------------------------------------------------------------------------------------------- //
  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    val bag = UiUtils.createDefaultGridBag().setDefaultWeightX(0.5).setDefaultFill(GridBagConstraints.HORIZONTAL)

    add(InnerLatestGradleReleasesPanel("Productive", productiveReleases, true), bag.nextLine().next())

    add(JPanel().apply {
      border = CompoundBorder(JBEmptyBorder(0, UIUtil.DEFAULT_HGAP, 0, 0),
                              CompoundBorder(CustomLineBorder(JBColor.border(), 0, 1, 0, 0),
                                             JBEmptyBorder(0, 0, 0, 0)))
    }, bag.next().weighty(1.0).weightx(0.0).fillCellVertically())

    add(InnerLatestGradleReleasesPanel("Pre Release", preReleaseReleases, false), bag.next())


    add(JPanel(), bag.nextLine().next().coverLine().fillCell())

    ApplicationManager.getApplication().invokeLater {
      requestFocusInWindow()
    }
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //
  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  private class InnerLatestGradleReleasesPanel(title: String, releases: List<GradleRelease>, productive: Boolean) : JPanel(GridBagLayout()) {

    init {
      val bag = UiUtils.createDefaultGridBag()

      add(JBLabel("<html><b>$title</b></html>").xxlFont(), bag.nextLine().next().weightx(1.0).fillCellHorizontally().overrideBottomInset(UIUtil.LARGE_VGAP))

      releases.forEachIndexed { index, release ->
        if (index != 0) {
          add(JSeparator(), bag.nextLine().next().weightx(1.0).fillCellHorizontally().overrideTopInset(UIUtil.LARGE_VGAP).overrideBottomInset(UIUtil.LARGE_VGAP))
        }

        add(JBLabel("<html><b>${release.name}</b></html>").xlFont(), bag.nextLine().next().weightx(1.0).fillCellHorizontally())

        val publishedDate = JBDateFormat.getFormatter().formatPrettyDateTime(release.publishedAt)
        val publishedSinceDays = ChronoUnit.DAYS.between(release.publishedAt.toInstant(), Instant.now())
        add(JBLabel("$publishedDate - $publishedSinceDays ${if (publishedSinceDays == 1L) "day" else "days"} ago"), bag.nextLine().next().overrideTopInset(UIUtil.DEFAULT_VGAP).weightx(1.0).fillCellHorizontally())

        val gitHubRelease = UiUtils.createLink("GitHub Release", "https://github.com/gradle/gradle/releases/tag/${release.gitHubTag}")
        if (productive) {
          add(UiUtils.createLink("Release Notes", "https://docs.gradle.org/${release.name}/release-notes.html"), bag.nextLine().next().overrideTopInset(UIUtil.LARGE_VGAP))
          add(UiUtils.createLink("User Manual", "https://docs.gradle.org/${release.name}/userguide/userguide.html"), bag.nextLine().next().overrideTopInset(UIUtil.DEFAULT_VGAP))
          add(gitHubRelease, bag.nextLine().next().overrideTopInset(UIUtil.DEFAULT_VGAP))
        }
        else {
          add(gitHubRelease, bag.nextLine().next().overrideTopInset(UIUtil.LARGE_VGAP))
        }

        add(JBLabel("Update Gradle wrapper:"), bag.nextLine().next().overrideTopInset(UIUtil.LARGE_VGAP))
        val gradleVersion = release.gitHubTag // Example: v6.2.0-RC2
                .substring(1) // Remove the 'v'
                .lowercase()
        val updateGradleWrapperCommand = "./gradlew wrapper --gradle-version=$gradleVersion"
        add(JBUI.Panels.simplePanel(JBTextField(updateGradleWrapperCommand).apply { isEditable = false; caretPosition = 0 }).apply {
          addToRight(UiUtils.createCopyButton { updateGradleWrapperCommand }.apply { border = JBEmptyBorder(0, 2, 0, 0) })
        }, bag.nextLine().next().overrideTopInset(2))
      }
    }
  }
}
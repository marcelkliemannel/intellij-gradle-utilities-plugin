package dev.turingcomplete.intellijgradleutilitiesplugin.verification

import com.intellij.icons.AllIcons
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.UIUtil
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.*
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.SwingConstants

class VerificationResultPanel(verificationResult: VerificationResult) : JPanel(GridBagLayout()) {
  // -- Companion Object -------------------------------------------------------------------------------------------- //
  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    val bag = UiUtils.createDefaultGridBag()

    if (verificationResult.fileVerificationResults.isEmpty()) {
      add(JBLabel("No files were verified.", SwingConstants.CENTER), bag.nextLine().next())
    }
    else {
      verificationResult.fileVerificationResults.map { createFileVerificationResultComponent(it) }.forEachIndexed { index, verificationResultComponent ->
        if (index != 0) {
          add(JSeparator(), bag.nextLine().next().fillCellHorizontally().overrideTopInset(UIUtil.LARGE_VGAP).overrideBottomInset(UIUtil.LARGE_VGAP))
        }

        add(verificationResultComponent, bag.nextLine().next())
      }
    }

    add(JSeparator(), bag.nextLine().next().fillCellHorizontally().overrideTopInset(UIUtil.LARGE_VGAP).overrideBottomInset(UIUtil.LARGE_VGAP))

    add(UiUtils.createLink("User manual: Verification of Gradle distributions and wrapper JAR",
                           "https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:verification"),
        bag.nextLine().next().overrideTopInset(UIUtil.DEFAULT_VGAP))

    add(UiUtils.createLink("All Gradle distribution and wrapper JAR checksums",
                           "https://gradle.org/release-checksums/"),
        bag.nextLine().next().overrideTopInset(UIUtil.DEFAULT_VGAP))

    val lastCellBag = bag.nextLine().next().weighty(1.0).weightx(1.0).fillCell()
    if (verificationResult.errors.isNotEmpty()) {
      add(ErrorsPanel(verificationResult.errors), lastCellBag.overrideTopInset(UIUtil.DEFAULT_HGAP))
    }
    else {
      add(JPanel(), lastCellBag)
    }
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //
  // -- Private Methods --------------------------------------------------------------------------------------------- //

  private fun createFileVerificationResultComponent(fileVerificationResult: VerificationResult.File): JComponent {

    return JPanel(GridBagLayout()).apply {
      val bag = UiUtils.createDefaultGridBag()

      val fileNameText = "<html><b>${fileVerificationResult.file.fileName}</b></html>"
      if (fileVerificationResult.actualChecksum == fileVerificationResult.expectedChecksum) {
        add(JBLabel(fileNameText, AllIcons.General.InspectionsOK, SwingConstants.LEFT), bag.nextLine().coverLine().next())
        add(JBLabel(fileVerificationResult.matchingMessage), bag.nextLine().next().coverLine().overrideTopInset(UIUtil.DEFAULT_VGAP))
        add(JBLabel("Checksum: ${fileVerificationResult.actualChecksum}").copyable(), bag.nextLine().next().coverLine().overrideTopInset(UIUtil.LARGE_VGAP))
      }
      else {
        add(JBLabel(fileNameText, AllIcons.Ide.FatalError, SwingConstants.LEFT), bag.nextLine().next().coverLine())
        add(JBLabel(fileVerificationResult.errorMessage), bag.nextLine().next().coverLine().overrideTopInset(UIUtil.DEFAULT_VGAP))
        add(JBLabel("Expected: ${fileVerificationResult.expectedChecksum}").copyable(), bag.nextLine().next().coverLine().overrideTopInset(UIUtil.LARGE_VGAP))
        add(JBLabel("Actual: ${fileVerificationResult.actualChecksum}").copyable(), bag.nextLine().next().coverLine().overrideTopInset(UIUtil.DEFAULT_VGAP))
      }

      add(UiUtils.createLink("Official checksum file", fileVerificationResult.checksumFileUrl), bag.nextLine().next().coverLine().overrideTopInset(UIUtil.DEFAULT_VGAP))

      fileVerificationResult.warnings.forEach { warning ->
        add(JBLabel(warning.text, AllIcons.General.Warning, SwingConstants.LEFT), bag.nextLine().next().overrideTopInset(UIUtil.DEFAULT_VGAP))
        add(UiUtils.createLink("Learn more", warning.learnMoreLink), bag.next().overrideLeftInset(2).overrideTopInset(UIUtil.DEFAULT_VGAP))
      }
    }
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
package dev.turingcomplete.intellijgradleutilitiesplugin.common.ui

import com.intellij.icons.AllIcons
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.panels.VerticalLayout
import com.intellij.util.ui.UIUtil
import javax.swing.JPanel
import javax.swing.SwingConstants

class ErrorsPanel(errors: List<String>) : JPanel(VerticalLayout(UIUtil.LARGE_VGAP)) {
  // -- Companion Object ---------------------------------------------------- //
  // -- Properties ---------------------------------------------------------- //
  // -- Initialization ------------------------------------------------------ //

  init {
    border = IdeBorderFactory.createTitledBorder("Errors")

    errors.forEach { error ->
      add(JBLabel("<html>$error</html>", AllIcons.Ide.FatalError, SwingConstants.LEFT))
    }

    add(
      JBLabel(
        "<html>Please see idea.log for details. If one of the errors should not have been happened, " +
          "please report a bug for the Gradle Utilities plugin.</html>",
        UIUtil.ComponentStyle.SMALL,
      )
    )
  }

  // -- Exported Methods ---------------------------------------------------- //
  // -- Private Methods ----------------------------------------------------- //
  // -- Inner Type ---------------------------------------------------------- //
}

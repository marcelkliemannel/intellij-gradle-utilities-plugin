package dev.turingcomplete.intellijgradleutilitiesplugin.managegradleuserhome

import com.intellij.openapi.actionSystem.DataContext
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtils
import dev.turingcomplete.intellijgradleutilitiesplugin.common.OpenDirectoryAction

class OpenGradleUserHome : OpenDirectoryAction() {
  // -- Companion Object ---------------------------------------------------- //
  // -- Properties ---------------------------------------------------------- //
  // -- Initialization ------------------------------------------------------ //
  // -- Exported Methods ---------------------------------------------------- //

  override fun directory(dataContext: DataContext) = GradleUtils.gradleUserHome()

  // -- Private Methods ----------------------------------------------------- //
  // -- Inner Type ---------------------------------------------------------- //
}

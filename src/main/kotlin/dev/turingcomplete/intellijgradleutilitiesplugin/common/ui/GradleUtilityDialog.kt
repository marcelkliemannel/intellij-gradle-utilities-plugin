package dev.turingcomplete.intellijgradleutilitiesplugin.common.ui

import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.Dimension
import javax.swing.JComponent

class GradleUtilityDialog private constructor(title: String,
                                                 project: Project?,
                                                 private val component: JComponent,
                                                 size: Dimension)
  : DialogWrapper(project), DataProvider {
  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    fun show(title: String, component: JComponent, size: Dimension, project: Project?) {
      GradleUtilityDialog(title, project, component, size).show()
    }
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    this.title = title
    setSize(size.width, size.height)
    setOKButtonText("Close")
    init()
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun createCenterPanel() = component

  override fun getData(dataId: String) : Any? {
    return when (component) {
      is DataProvider -> component.getData(dataId)
      else -> null
    }
  }

  override fun createActions() = arrayOf(myOKAction)

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
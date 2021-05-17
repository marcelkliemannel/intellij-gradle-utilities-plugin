package dev.turingcomplete.intellijgradleutilitiesplugin.common.ui

import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.Dimension
import javax.swing.Action
import javax.swing.JComponent

class GradleUtilityDialog private constructor(title: String,
                                              project: Project?,
                                              private val createComponent: () -> JComponent,
                                              size: Dimension)
  : DialogWrapper(project), DataProvider {
  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    fun show(title: String, createComponent: () -> JComponent, size: Dimension, project: Project?) {
      GradleUtilityDialog(title, project, createComponent, size).show()
    }
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //

  private val component by lazy { createComponent() }

  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    this.title = title
    setSize(size.width, size.height)
    setOKButtonText("Close")
    init()
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun createCenterPanel() = component

  override fun show() {
    val component0 = component
    if (component0 is DialogHandler) {
      component0.onDialogShow()
    }

    super.show()
  }

  override fun getCancelAction(): Action {
    return super.getCancelAction()
  }

  override fun getData(dataId: String) : Any? {
    return when (val component0 = component) {
      is DataProvider -> component0.getData(dataId)
      else -> null
    }
  }

  override fun createActions() = arrayOf(myOKAction)

  // -- Private Methods --------------------------------------------------------------------------------------------- //

  interface DialogHandler {

    fun onDialogShow()
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
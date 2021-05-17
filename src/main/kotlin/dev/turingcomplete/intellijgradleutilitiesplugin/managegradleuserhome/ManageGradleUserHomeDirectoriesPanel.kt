package dev.turingcomplete.intellijgradleutilitiesplugin.managegradleuserhome

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.DumbAwareToggleAction
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBUI
import dev.turingcomplete.intellijgradleutilitiesplugin.common.*
import dev.turingcomplete.intellijgradleutilitiesplugin.common.CommonDataKeys
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.ManageEntriesPanel
import org.apache.commons.io.FileUtils
import javax.swing.SwingConstants

abstract class ManageGradleUserHomeDirectoriesPanel(columns: List<Column<Directory>>,
                                                    collectEntriesAction: GradleUtilityAction<List<Directory>>,
                                                    private val statusTextCollectingEntries: String,
                                                    statusTextNoEntries: String)
  : ManageEntriesPanel<Directory>(columns.plus(SIZE_COLUMN), collectEntriesAction, statusTextNoEntries), DataProvider {

  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private val SIZE_COLUMN = Column<Directory>("Size") {
      if (it.size != null) FileUtils.byteCountToDisplaySize(it.size) else null
    }

    private const val TOTAL_SIZE_UNKNOWN_TEXT = "Total size: unknown"
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //

  private val contextMenuAction: ActionGroup = createContextMenuAction()
  private var calculateSizeSelected: Boolean = false
  private var totalSizeLabel = JBLabel(TOTAL_SIZE_UNKNOWN_TEXT, SwingConstants.LEFT)

  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    collectEntriesAction
            .onBeforeStart {
              totalSizeLabel.text = TOTAL_SIZE_UNKNOWN_TEXT
              totalSizeLabel.isEnabled = false
            }
            .onSuccess { directories ->
              if (calculateSizeSelected) {
                val totalSize = directories!!.sumOf { directory -> directory.size ?: return@onSuccess }
                totalSizeLabel.text = "Total size: ${FileUtils.byteCountToDisplaySize(totalSize)}"
              }
            }
            .onFinished { totalSizeLabel.isEnabled = true }


    @Suppress("LeakingThis")
    addToBottom(JBUI.Panels.simplePanel(totalSizeLabel).apply {
      border = JBEmptyBorder(2, 0, 0, 0)
    })

    init()
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun doGetData(dataId: String): Any? {
    return when {
      CollectDirectoriesAction.CALCULATE_SIZE.`is`(dataId) -> calculateSizeSelected
      CommonDataKeys.ALL_DIRECTORIES.`is`(dataId) -> allEntries()
      CommonDataKeys.SELECTED_DIRECTORIES.`is`(dataId) -> selectedEntries()
      CommonDataKeys.SELECTED_DIRECTORY.`is`(dataId) -> selectedEntry()
      else -> super.doGetData(dataId)
    }
  }

  override fun statusTextCollectingEntries(): Pair<String, String?> {
    return if (calculateSizeSelected) {
      Pair(statusTextCollectingEntries, "Calculating the size may take a moment")
    }
    else {
      Pair(statusTextCollectingEntries, null)
    }
  }

  override fun createManageAllEntriesActions() = listOf(DeleteAllDirectoriesAction().apply {
    isVisible = { true }
    onSuccess { collectEntries() }
    onFailure { collectEntries() }

    confirmationText = "Before deleting all directories all Gradle daemons " +
                       "should be terminated, otherwise the deletion might fail. " +
                       "Do you want to proceed?"
  })

  override fun createSettingsActions(): List<ToggleAction> = listOf(CalculateSizeToggleAction())

  override fun tableContextMenuActions(): ActionGroup? = contextMenuAction

  // -- Private Methods --------------------------------------------------------------------------------------------- //

  private fun createContextMenuAction() = DefaultActionGroup().apply {
    add(OpenDirectoryAction())
    add(DeleteDirectoriesAction().apply {
      confirmationText = "Before deleting the selected directory/directories all Gradle daemons " +
                         "should be terminated, otherwise the deletion might fail. " +
                         "Do you want to proceed?"

      onSuccess { collectEntries() }
      onFailure { collectEntries() }
    })
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  private inner class CalculateSizeToggleAction
    : DumbAwareToggleAction("Calculate Size", null, AllIcons.Debugger.EvaluateExpression) {

    override fun isSelected(e: AnActionEvent): Boolean = calculateSizeSelected

    override fun setSelected(e: AnActionEvent, state: Boolean) {
      calculateSizeSelected = !calculateSizeSelected
      if (calculateSizeSelected) {
        collectEntries()
      }
    }
  }
}
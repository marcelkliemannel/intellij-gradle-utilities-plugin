package dev.turingcomplete.intellijgradleutilitiesplugin.common.ui

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.JBColor
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.UiUtils.Table.formatCell
import java.awt.Component
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableModel
import kotlin.properties.Delegates

/**
 * Overriders must call [ManageEntriesPanel#init] at the end of their
 * constructors.
 */
abstract class ManageEntriesPanel<E>(columns: List<Column<E>>,
                                     private val collectEntriesAction: GradleUtilityAction<List<E>>,
                                     private val statusTextNoEntries: String)
  : BorderLayoutPanel(), DataProvider, GradleUtilityDialog.DialogHandler {

  // -- Companion Object -------------------------------------------------------------------------------------------- //
  // -- Properties -------------------------------------------------------------------------------------------------- //

  private val toolbar : JComponent by lazy { createToolbar() }
  private val tableModel = ManageEntriesModel(columns)
  protected val table = ManageEntriesTable(tableModel)

  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  protected fun init() {
    initCollectEntriesAction()

    addToCenter(SimpleToolWindowPanel(true).apply {
      border = JBUI.Borders.customLine(JBColor.border())
      toolbar = this@ManageEntriesPanel.toolbar
      setContent(ScrollPaneFactory.createScrollPane(table, true))
    })

    syncGui()
  }

  final override fun getData(dataId: String): Any? {
    return when {
      PlatformDataKeys.CONTEXT_COMPONENT.`is`(dataId) -> this
      else -> doGetData(dataId)
    }
  }

  open fun doGetData(dataId: String): Any? = null

  abstract fun statusTextCollectingEntries() : Pair<String, String?>

  protected open fun createManageAllEntriesActions(): List<GradleUtilityAction<*>> = listOf()

  protected open fun createSettingsActions(): List<ToggleAction> = listOf()

  /**
   * The method is called every time the context menu is opened.
   */
  protected open fun tableContextMenuActions(): ActionGroup? = null

  protected fun collectEntries() {
    collectEntriesAction.execute(this)
  }

  protected fun allEntries(): List<E> = tableModel.entries

  protected fun selectedEntry(): E? {
    val selectedDaemons = selectedEntries()
    return if (selectedDaemons.size == 1) selectedDaemons[0] else null
  }

  protected fun selectedEntries(): List<E> {
    val runningGradleDaemons = tableModel.entries
    return table.selectedRows.map { runningGradleDaemons[it] }.toCollection(mutableListOf())
  }

  override fun onDialogShow() {
    collectEntries()
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //

  private fun createToolbar(): JComponent {
    val actionGroup = DefaultActionGroup().apply {
      add(collectEntriesAction)

      val manageAllEntriesActions = createManageAllEntriesActions()
      if (manageAllEntriesActions.isNotEmpty()) {
        addSeparator()

        manageAllEntriesActions.forEach {
          it.apply { isEnabled = { actionOnEntriesEnabled() } }
                  .onSuccess { collectEntries() }
                  .onFailure { collectEntries() }
          add(it)
        }
      }

      val settingsActions = createSettingsActions()
      if (settingsActions.isNotEmpty()) {
        addSeparator()

        settingsActions.forEach { add(it) }
      }
    }

    return ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, true).component
  }

  private fun actionOnEntriesEnabled(): Boolean {
    return if (collectEntriesAction.isInExecution()) false else tableModel.entries.isNotEmpty()
  }

  private fun initCollectEntriesAction() {
    collectEntriesAction
            .onBeforeStart {
              tableModel.entries = listOf()
              ApplicationManager.getApplication().invokeLater {
                syncGui()
              }
            }
            .onSuccess { result ->
              tableModel.entries = result ?: throw IllegalStateException("snh: Result not set")
            }
            .onFinished {
              syncGui()
            }
  }

  private fun syncGui() {
    val isInExecution = collectEntriesAction.isInExecution()

    UIUtil.setEnabled(table, !isInExecution, true)

    table.emptyText.clear()
    if (isInExecution) {
      val statusTextCollectingEntries = statusTextCollectingEntries()
      table.emptyText.text = statusTextCollectingEntries.first
      val secondLine = statusTextCollectingEntries.second
      if (secondLine != null) {
        table.emptyText.appendLine(secondLine)
      }
    }
    else {
      table.emptyText.text = statusTextNoEntries
    }

    toolbar.apply {
      revalidate()
      repaint()
    }

    revalidate()
    repaint()
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  class Column<E>(val title: String, val value: (E) -> String?)

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  private class ManageEntriesModel<E>(private val columns: List<Column<E>>) : AbstractTableModel() {

    override fun getColumnCount(): Int = columns.size

    var entries: List<E> by Delegates.observable(listOf()) { _, _, _ -> fireTableDataChanged() }

    override fun getColumnName(column: Int): String = columns[column].title

    override fun getRowCount(): Int = entries.size

    override fun getValueAt(row: Int, column: Int): Any? = columns[column].value(entries[row])
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  protected inner class ManageEntriesTable(tableModel: TableModel) : JBTable(tableModel) {

    init {
      addMouseListener(UiUtils.Table.createContextMenuMouseListener { tableContextMenuActions() })

      setDefaultRenderer(Object::class.java, object : JBLabel(), TableCellRenderer {
        override fun getTableCellRendererComponent(table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
          text = if (value == null) "Unknown" else (value as String)
          font = if (value == null) UIUtil.getFont(UIUtil.FontSize.SMALL, UIUtil.getLabelFont()) else UIUtil.getLabelFont()
          return this.formatCell(table, isSelected)
        }
      })
    }
  }
}

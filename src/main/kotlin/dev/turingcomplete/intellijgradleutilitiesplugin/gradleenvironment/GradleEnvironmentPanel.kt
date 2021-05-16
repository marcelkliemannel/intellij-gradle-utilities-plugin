package dev.turingcomplete.intellijgradleutilitiesplugin.gradleenvironment

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ide.actions.RevealFileAction
import com.intellij.idea.ActionsBundle
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.HyperlinkLabel
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.*
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.UiUtils.Table.formatCell
import java.awt.Component
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.datatransfer.StringSelection
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.nio.file.Path
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableCellRenderer


class GradleEnvironmentPanel(private val gradleEnvironment: GradleEnvironment) : JPanel(GridBagLayout()) {
  // -- Companion Object -------------------------------------------------------------------------------------------- //
  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    val bag = UiUtils.createDefaultGridBag().setDefaultWeightX(1.0).setDefaultFill(GridBagConstraints.HORIZONTAL)

    add(createPathsComponent(), bag.nextLine().next())

    add(createSystemGradleComponent(), bag.nextLine().next().overrideTopInset(UIUtil.DEFAULT_HGAP))

    add(createGradleWrapperComponent(), bag.nextLine().next().overrideTopInset(UIUtil.DEFAULT_HGAP))

    add(JBLabel("Project Gradle properties:"), bag.nextLine().next().overrideTopInset(UIUtil.DEFAULT_HGAP))
    add(createPropertiesTable(gradleEnvironment.projectProperties, listOf("Key", "Value"), "Project does not have any Gradle properties set"), bag.nextLine().next().weighty(0.4).fillCell().overrideTopInset(2))

    add(JBLabel("User Gradle properties:"), bag.nextLine().next().overrideTopInset(UIUtil.LARGE_VGAP))
    add(createPropertiesTable(gradleEnvironment.userProperties, listOf("Key", "Value"), "User does not have any Gradle properties set"), bag.nextLine().next().weighty(0.4).fillCell().overrideTopInset(2))

    add(JBLabel("Gradle related environment variables:"), bag.nextLine().next().overrideTopInset(UIUtil.LARGE_VGAP))
    add(createPropertiesTable(gradleEnvironment.environmentVariables, listOf("Name", "Value"), "No Gradle related environment variables", "Environment Variable", "Environment Variables"), bag.nextLine().next().weighty(0.4).fillCell().overrideTopInset(2))

    val lastCellBag = bag.nextLine().next().weighty(1.0).weightx(1.0).coverLine().fillCell()
    if (gradleEnvironment.errors.isNotEmpty()) {
      add(ErrorsPanel(gradleEnvironment.errors), lastCellBag.overrideTopInset(UIUtil.DEFAULT_HGAP))
    }
    else {
      add(JPanel(), lastCellBag)
    }
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //
  // -- Private Methods --------------------------------------------------------------------------------------------- //

  private fun createPathsComponent(): JComponent {
    return JPanel(GridBagLayout()).apply {

      val bag = UiUtils.createDefaultGridBag()

      add(JBLabel("Gradle user home:"), bag.nextLine().next())
      add(createDirHyperlink(gradleEnvironment.gradleUserHomeDir), bag.next().weightx(1.0).fillCellHorizontally().overrideLeftInset(4))
    }
  }

  private fun createDirHyperlink(dir: Path): HyperlinkLabel {
    return HyperlinkLabel(dir.toString()).apply {
      toolTipText = if (SystemInfo.isMac) {
        ActionsBundle.message("action.RevealIn.name.mac")
      }
      else {
        ActionsBundle.message("action.RevealIn.name.other", RevealFileAction.getFileManagerName())
      }

      addHyperlinkListener {
        BrowserUtil.browse(dir)
      }
    }
  }

  private fun createSystemGradleComponent(): JComponent {
    return JPanel(GridBagLayout()).apply {

      border = IdeBorderFactory.createTitledBorder("System Gradle")

      val bag = UiUtils.createDefaultGridBag()

      val gradleSystem = gradleEnvironment.systemGradle
      if (gradleSystem == null) {
        add(JBLabel("Couldn't find Gradle installation on this system."), bag.nextLine().next())
      }
      else {
        add(JBLabel("Gradle home:"), bag.nextLine().next())
        add(createDirHyperlink(gradleSystem.gradleHomeDir), bag.next().weightx(1.0).fillCellHorizontally().overrideLeftInset(4))

        add(JBLabel("Version:"), bag.nextLine().next().overrideTopInset(UIUtil.DEFAULT_VGAP))
        add(JBLabel(gradleSystem.version ?: "Unknown").copyable(), bag.next().weightx(1.0).fillCellHorizontally().overrideLeftInset(4).overrideTopInset(UIUtil.DEFAULT_VGAP))
      }
    }
  }

  private fun createGradleWrapperComponent(): JComponent {
    return JPanel(GridBagLayout()).apply {

      border = IdeBorderFactory.createTitledBorder("Gradle Wrapper")

      val bag = UiUtils.createDefaultGridBag()

      val gradlewWrapper = gradleEnvironment.gradleWrapper
      if (gradlewWrapper == null) {
        add(JBLabel("Couldn't find Gradle wrapper JAR and/or executable in project."), bag.nextLine().next())
      }
      else {
        add(JBLabel("Version:"), bag.nextLine().next())
        add(JBLabel(gradlewWrapper.version ?: "Unknown").copyable(), bag.next().weightx(1.0).fillCellHorizontally().overrideLeftInset(4))

        add(JBLabel("Checksum:"), bag.nextLine().next().overrideTopInset(UIUtil.DEFAULT_VGAP))
        add(JBLabel(gradlewWrapper.checksum ?: "Unknown").copyable(), bag.next().weightx(1.0).fillCellHorizontally().overrideLeftInset(4).overrideTopInset(UIUtil.DEFAULT_VGAP))

        add(JBLabel("Gradle wrapper properties:"), bag.nextLine().next().coverLine().overrideTopInset(UIUtil.DEFAULT_VGAP))
        add(createPropertiesTable(gradlewWrapper.wrapperProperties, listOf("Key", "Value"), "No Gradle wrapper proeprties"), bag.nextLine().next().coverLine().weighty(0.4).fillCell().overrideTopInset(2))
      }
    }
  }

  private fun createPropertiesTable(data: List<Pair<String, String?>>,
                                    columnNames: List<String>,
                                    noDataText: String,
                                    singleDataName: String = "Property",
                                    pluralDataName: String = "Properties"): JComponent {

    return BorderLayoutPanel().apply {
      minimumSize = Dimension(minimumSize.width, 80)
      preferredSize = Dimension(preferredSize.width, 100)

      val tableModel = PropertiesTableModel(data, columnNames)
      val table = PropertiesTable(tableModel, noDataText, singleDataName, pluralDataName)
      table.columnModel.getColumn(1).preferredWidth = 250
      addToCenter(JBScrollPane(table))
    }
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  private class PropertiesTableModel(private val data: List<Pair<String, String?>>,
                                     private val columnNames: List<String>) : AbstractTableModel() {
    init {
      assert(columnNames.size == 2)
    }

    override fun getColumnCount(): Int = 2

    override fun getColumnName(column: Int) = columnNames[column]

    override fun getRowCount(): Int = data.size

    fun getRow(row: Int): Pair<String, String?> = data[row]

    override fun getValueAt(row: Int, column: Int) = data[row].let {
      if (column == 0) it.first else it.second
    }
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  private class PropertiesTable(private val tableModel: PropertiesTableModel,
                                noDataText: String,
                                singleDataName: String,
                                pluralDataName: String) : JBTable(tableModel), DataProvider {

    companion object {
      val SELECTED_PROPERTIES_DATA_KEY = DataKey.create<List<Pair<String, String?>>>("selectedProperties")
    }

    init {
      emptyText.text = noDataText

      setDefaultRenderer(Object::class.java, object : JBLabel(), TableCellRenderer {
        override fun getTableCellRendererComponent(table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
          text = if (value == null) "Not set" else (value as String)
          font = if (value == null) UIUtil.getFont(UIUtil.FontSize.SMALL, UIUtil.getLabelFont()) else UIUtil.getLabelFont()
          return this.formatCell(table, isSelected)
        }
      })

      val contextMenuActions = DefaultActionGroup(CopyAction(singleDataName, pluralDataName), ExpandValue(singleDataName))
      addMouseListener(UiUtils.Table.createContextMenuMouseListener { contextMenuActions })

      addFocusListener(object : FocusListener {
        override fun focusGained(e: FocusEvent?) {
          // Nothing to do
        }

        override fun focusLost(e: FocusEvent?) {
          selectionModel.clearSelection()
        }
      })
    }

    override fun getData(dataId: String): Any? {
      if (SELECTED_PROPERTIES_DATA_KEY.`is`(dataId)) {
        return selectedRows.map { tableModel.getRow(it) }.toList()
      }

      return null
    }
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  private class CopyAction(private val singleDataName: String, private val pluralDataName: String)
    : DumbAwareAction("Copy Value", null, AllIcons.Actions.Copy) {

    override fun update(e: AnActionEvent) {
      val selectedProperties = PropertiesTable.SELECTED_PROPERTIES_DATA_KEY.getData(e.dataContext)
                               ?: throw IllegalStateException("snh: Data missing")

      val nonNullSelectedProperties = selectedProperties.filter { it.second != null }
      e.presentation.isVisible = nonNullSelectedProperties.isNotEmpty()
      e.presentation.text = if (nonNullSelectedProperties.size > 1) {
        "Copy ${nonNullSelectedProperties.size} $pluralDataName"
      }
      else {
        "Copy $singleDataName"
      }
    }

    override fun actionPerformed(e: AnActionEvent) {
      val selectedProperties = PropertiesTable.SELECTED_PROPERTIES_DATA_KEY.getData(e.dataContext)
                               ?: throw IllegalStateException("snh: Data missing")
      if (selectedProperties.isEmpty()) {
        return
      }

      val textToCopy = selectedProperties.joinToString("\n") { "${it.first}=${it.second}" }
      CopyPasteManager.getInstance().setContents(StringSelection(textToCopy))
    }
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  private class ExpandValue(private val singleDataName: String)
    : DumbAwareAction("Expand Value", null, AllIcons.General.ExpandComponent) {

    override fun update(e: AnActionEvent) {
      val selectedProperties = PropertiesTable.SELECTED_PROPERTIES_DATA_KEY.getData(e.dataContext)
                               ?: throw IllegalStateException("snh: Data missing")

      e.presentation.isVisible = selectedProperties.size == 1 && selectedProperties[0].second != null
    }

    override fun actionPerformed(e: AnActionEvent) {
      val selectedProperties = PropertiesTable.SELECTED_PROPERTIES_DATA_KEY.getData(e.dataContext)
                               ?: throw IllegalStateException("snh: Data missing")
      if (selectedProperties.size != 1 && selectedProperties[0].second != null) {
        return
      }

      val selectedProperty = selectedProperties[0]
      val valueTextAreaPanel = UiUtils.Component.TextAreaPanel(selectedProperty.second ?: "")

      JBPopupFactory.getInstance()
              .createComponentPopupBuilder(valueTextAreaPanel, valueTextAreaPanel)
              .setRequestFocus(true)
              .setTitle("Value of $singleDataName: ${selectedProperty.first}")
              .setFocusable(true)
              .setResizable(true)
              .setMovable(true)
              .setModalContext(false)
              .setShowShadow(true)
              .setShowBorder(false)
              .setCancelKeyEnabled(true)
              .setCancelOnClickOutside(true)
              .setCancelOnOtherWindowOpen(false)
              .createPopup()
              .showInBestPositionFor(e.dataContext)
    }
  }
}
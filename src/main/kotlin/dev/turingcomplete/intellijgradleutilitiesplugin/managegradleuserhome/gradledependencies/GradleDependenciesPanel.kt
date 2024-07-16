package dev.turingcomplete.intellijgradleutilitiesplugin.managegradleuserhome.gradledependencies

import com.github.weisj.jsvg.r
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBUI
import dev.turingcomplete.intellijgradleutilitiesplugin.common.CollectDirectoriesAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.CommonDataKeys
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.GradleUtilitiesPluginUtils
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.ManageEntriesPanel
import dev.turingcomplete.intellijgradleutilitiesplugin.managegradleuserhome.gradledependencies.action.DeleteDependencyAction
import org.apache.commons.io.FileUtils
import java.awt.BorderLayout
import java.awt.GridBagLayout
import java.awt.Panel
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingConstants

class GradleDependenciesPanel(collectEntriesAction: CollectGradleDependenciesAction = CollectGradleDependenciesAction()) :
  ManageEntriesPanel<GradleDep>(COLUMNS, collectEntriesAction, "No Gradle dependencies", "${GradleUtilitiesPluginUtils.TOOLBAR_PLACE_PREFIX}.gradledependencies"),
    DataProvider {

  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private const val STATUS_TEXT_COLLECTING = "Collecting Gradle dependencies..."
      private const val TOTAL_SIZE_UNKNOWN_TEXT = "Total size: unknown"

    private val COLUMNS = listOf<Column<GradleDep>>(
        Column("Group") { it.group },
        Column("Name") { it.name },
        Column("Version") { it.version },
        Column("Size") { it.size?.let { it1 -> FileUtils.byteCountToDisplaySize(it1) } }
    )
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
            .onSuccess { deps ->
                if (calculateSizeSelected) {
                    val totalSize = deps!!.sumOf { directory -> directory.size ?: return@onSuccess }
                    totalSizeLabel.text = "Total size: ${FileUtils.byteCountToDisplaySize(totalSize)}"
                }
            }
            .onFinished { totalSizeLabel.isEnabled = true }

        addToBottom(JBUI.Panels.simplePanel(totalSizeLabel).apply {
            border = JBEmptyBorder(2, 0, 0, 0)
        })

        // search
        addToTop(JPanel().apply {
            layout = BorderLayout()
            val text: JBTextField = JBTextField().apply {
                addKeyListener(object : KeyAdapter() {
                    override fun keyPressed(e: KeyEvent?) {
                        if (e?.keyCode == KeyEvent.VK_ENTER) {
                            collectEntriesAction.searchFilter = text.trim()
                            collectEntries()
                            e.consume()
                        }
                    }
                })
            }
            add(text, BorderLayout.CENTER)
            add(JButton("Search").apply {
                addActionListener {
                    collectEntriesAction.searchFilter = text.text.trim()
                    collectEntries()
                }
            }, BorderLayout.EAST)
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
            Pair(STATUS_TEXT_COLLECTING, "Calculating the size may take a moment")
        }
        else {
            Pair(STATUS_TEXT_COLLECTING, null)
        }
    }

    override fun createSettings(): List<Setting> = listOf(CalculateSizeSetting())

    override fun tableContextMenuActions(): ActionGroup? = contextMenuAction


    // -- Private Methods --------------------------------------------------------------------------------------------- //

    private fun createContextMenuAction() = DefaultActionGroup().apply {
        add(DeleteDependencyAction().apply {
            confirmationText = "Before deleting the selected dependency/dependencies all Gradle daemons " +
                    "should be terminated, otherwise the deletion might fail. " +
                    "Do you want to proceed?"
        }.onFinished {
            collectEntries()
        })
    }

    // -- Inner Type -------------------------------------------------------------------------------------------------- //


    private inner class CalculateSizeSetting : Setting("Calculate size") {

        override fun isSelected(): Boolean = calculateSizeSelected

        override fun setSelected(selected: Boolean) {
            val oldState = calculateSizeSelected
            calculateSizeSelected = selected
            if (calculateSizeSelected && !oldState) {
                collectEntries()
            }
        }
    }
}
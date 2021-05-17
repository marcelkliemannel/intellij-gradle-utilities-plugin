package dev.turingcomplete.intellijgradleutilitiesplugin.runninggradledaemon

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.DumbAwareToggleAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.ManageEntriesPanel

class RunningGradleDaemonsPanel
  : ManageEntriesPanel<GradleDaemon>(COLUMNS,
                                     CollectRunningGradleDaemonsAction(),
                                     "No running Gradle daemons") {

  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private val COLUMNS = listOf<Column<GradleDaemon>>(Column("PID") { it.processInfo.pid.toString() },
                                                       Column("Version") { it.version },
                                                       Column("Status") { it.status })

    val ALL_DAEMONS: DataKey<List<GradleDaemon>> = DataKey.create("Gradle.Utilities.Plugin.RunningGradleDaemons.AllDaemons")
    val SELECTED_DAEMONS: DataKey<List<GradleDaemon>> = DataKey.create("Gradle.Utilities.Plugin.RunningGradleDaemons.SelectedDaemons")
    val SELECTED_DAEMON: DataKey<GradleDaemon> = DataKey.create("Gradle.Utilities.Plugin.RunningGradleDaemons.SelectedDaemon")
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //

  private var determineDaemonStatusSelected = false
  private val contextMenuActions: ActionGroup = createContextMenuActions()

  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    table.columnModel.getColumn(1).preferredWidth = 250

    init()
  }

  // -- Exported Methods -------------------------------------------------------------------------------------------- //

  override fun createManageAllEntriesActions() = listOf(TerminateGradleDaemonsAction.GracefullyAll(),
                                                        TerminateGradleDaemonsAction.KillAll())

  override fun tableContextMenuActions(): ActionGroup = contextMenuActions

  override fun doGetData(dataId: String): Any? {
    return when {
      ALL_DAEMONS.`is`(dataId) -> allEntries()
      SELECTED_DAEMONS.`is`(dataId) -> selectedEntries()
      SELECTED_DAEMON.`is`(dataId) -> selectedEntry()
      CollectRunningGradleDaemonsAction.DETERMINE_DAEMON_STATUS.`is`(dataId) -> determineDaemonStatusSelected
      else -> super.doGetData(dataId)
    }
  }

  override fun statusTextCollectingEntries(): Pair<String, String?> {
    return if (determineDaemonStatusSelected) {
      Pair("Collecting running Gradle daemons...", "Determining the status may take a moment")
    }
    else {
      Pair("Collecting running Gradle daemons...", null)
    }
  }

  override fun createSettingsActions(): List<ToggleAction> = listOf(CollectDaemonStatusToggleAction())

  // -- Private Methods --------------------------------------------------------------------------------------------- //

  private fun createContextMenuActions(): ActionGroup {
    return DefaultActionGroup().apply {
      add(TerminateGradleDaemonsAction.GracefullySelected().onSuccess { collectEntries() })
      add(TerminateGradleDaemonsAction.KillSelected().onSuccess { collectEntries() })

      addSeparator()

      add(ShowGradleDaemonCommandLineAction())
      add(OpenGradleDaemonLogAction())
    }
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  private inner class CollectDaemonStatusToggleAction
    : DumbAwareToggleAction("Collect Daemon Status", null, AllIcons.General.ShowInfos) {

    override fun isSelected(e: AnActionEvent): Boolean = determineDaemonStatusSelected

    override fun setSelected(e: AnActionEvent, state: Boolean) {
      determineDaemonStatusSelected = !determineDaemonStatusSelected
      if (determineDaemonStatusSelected) {
        collectEntries()
      }
    }
  }
}
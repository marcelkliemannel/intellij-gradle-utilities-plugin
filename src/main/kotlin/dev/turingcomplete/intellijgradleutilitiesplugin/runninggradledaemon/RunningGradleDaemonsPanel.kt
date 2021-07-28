package dev.turingcomplete.intellijgradleutilitiesplugin.runninggradledaemon

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.util.text.StringUtil
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.GradleUtilitiesPluginUtils
import dev.turingcomplete.intellijgradleutilitiesplugin.common.ui.ManageEntriesPanel

class RunningGradleDaemonsPanel
  : ManageEntriesPanel<GradleDaemon>(COLUMNS,
                                     CollectRunningGradleDaemonsAction(),
                                     "No running Gradle daemons",
                                     "${GradleUtilitiesPluginUtils.TOOLBAR_PLACE_PREFIX}.runninggradledaemons") {

  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    private val COLUMNS = listOf<Column<GradleDaemon>>(Column("PID") { it.pid.toString() },
                                                       Column("Version") { it.version },
                                                       Column("Status") { it.status },
                                                       Column("Uptime") { formatUptime(it.uptimeMillis) })

    private fun formatUptime(uptimeMillis: Long?): String {
      return uptimeMillis?.let { StringUtil.formatDuration(it) } ?: "Unknown"
    }

    val ALL_DAEMONS: DataKey<List<GradleDaemon>> = DataKey.create("Gradle.Utilities.Plugin.RunningGradleDaemons.AllDaemons")
    val SELECTED_DAEMONS: DataKey<List<GradleDaemon>> = DataKey.create("Gradle.Utilities.Plugin.RunningGradleDaemons.SelectedDaemons")
    val SELECTED_DAEMON: DataKey<GradleDaemon> = DataKey.create("Gradle.Utilities.Plugin.RunningGradleDaemons.SelectedDaemon")
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //

  private var determineDaemonStatusSelected = false
  private val contextMenuActions: ActionGroup = createContextMenuActions()

  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    table.columnModel.getColumn(1).preferredWidth = 180
    table.columnModel.getColumn(3).preferredWidth = 180

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

  override fun createSettings(): List<Setting> = listOf(CollectDaemonStatusSetting())

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

  private inner class CollectDaemonStatusSetting
    : Setting("Collect daemon status",
              "Collects the status of the daemons by parsing the output of the --status Gradle command. " +
              "For that the Gradle wrapper from the project and the system Gradle installation will be executed " +
              "(if these are available).<br /><br />" +
              "Because of the slow Gradle cold start, collecting the statuses may take a moment.") {

    override fun isSelected(): Boolean = determineDaemonStatusSelected

    override fun setSelected(selected: Boolean) {
      val oldState = determineDaemonStatusSelected
      determineDaemonStatusSelected = selected
      if (determineDaemonStatusSelected && oldState != determineDaemonStatusSelected) {
        collectEntries()
      }
    }
  }
}
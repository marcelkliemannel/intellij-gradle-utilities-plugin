package dev.turingcomplete.intellijgradleutilitiesplugin.common

import com.intellij.openapi.actionSystem.DataKey

object CommonDataKeys {
  // -- Properties -------------------------------------------------------------------------------------------------- //

  val ALL_DIRECTORIES: DataKey<List<Directory>> = DataKey.create("Gradle.Utilities.Plugin.CommonDataKeys.AllDirectories")
  val SELECTED_DIRECTORIES: DataKey<List<Directory>> = DataKey.create("Gradle.Utilities.Plugin.CommonDataKeys.SelectedDirectories")
  val SELECTED_DIRECTORY: DataKey<Directory> = DataKey.create("Gradle.Utilities.Plugin.CommonDataKeys.SelectedDirectory")

  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //
  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
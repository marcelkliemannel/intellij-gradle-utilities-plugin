package dev.turingcomplete.intellijgradleutilitiesplugin.common

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.util.NlsActions
import com.intellij.util.castSafelyTo

class DeleteAllDirectoriesAction(title: @NlsActions.ActionText String = "Delete All Directories",
                                 description: @NlsActions.ActionDescription String? = null)
  : DeleteDirectoriesAction(title, description) {

  // -- Companion Object -------------------------------------------------------------------------------------------- //
  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun directories(dataContext: DataContext): List<Directory> {
    return CommonDataKeys.ALL_DIRECTORIES.getData(dataContext)?.castSafelyTo<List<Directory>>() ?: listOf()
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
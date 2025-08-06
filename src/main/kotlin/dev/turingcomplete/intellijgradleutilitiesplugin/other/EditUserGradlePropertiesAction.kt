package dev.turingcomplete.intellijgradleutilitiesplugin.other

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessProvider
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFileManager
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityAction
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtilityActionFailedException
import dev.turingcomplete.intellijgradleutilitiesplugin.common.GradleUtils
import java.nio.file.Files
import java.nio.file.StandardOpenOption

class EditUserGradlePropertiesAction :
  GradleUtilityAction<Void>(
    "Edit User Gradle Properties",
    "Opens the user Gradle properties file in editor.",
    executionMode = ExecutionMode.DIRECT,
  ),
  DumbAware {

  // -- Companion Object ---------------------------------------------------- //

  companion object {
    private val LOG = Logger.getInstance(EditUserGradlePropertiesAction::class.java)
  }

  // -- Variables ----------------------------------------------------------- //
  // -- Initialization ------------------------------------------------------ //

  init {
    showOpensDialogIndicatorOnButtonText = false
  }

  // -- Exposed Methods ----------------------------------------------------- //

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun runAction(executionContext: ExecutionContext, progressIndicator: ProgressIndicator) {
    val project = CommonDataKeys.PROJECT.getData(executionContext.dataContext) ?: return

    val userGradlePropertiesFile = GradleUtils.gradleUserHome().resolve("gradle.properties")
    if (!Files.exists(userGradlePropertiesFile)) {
      var createFile = false
      ApplicationManager.getApplication().invokeAndWait {
        createFile =
          MessageDialogBuilder.yesNoCancel(
              "Create User Gradle Properties File",
              "<html>The user Gradle properties file:<br />" +
                "<b>$userGradlePropertiesFile</b><br />" +
                "does not exist. Should an empty file be created?",
            )
            .show(executionContext.project) == Messages.YES
      }

      if (!createFile) {
        return
      }

      LOG.info("Creating empty user Gradle properties file '${userGradlePropertiesFile}'...")
      NonProjectFileWritingAccessProvider.disableChecksDuring {
        Files.writeString(userGradlePropertiesFile, "", StandardOpenOption.CREATE)
      }
    }

    ApplicationManager.getApplication().invokeLater {
      val userGradlePropertiesVf =
        VirtualFileManager.getInstance().refreshAndFindFileByNioPath(userGradlePropertiesFile)
          ?: throw GradleUtilityActionFailedException(
            "User Gradle properties file '${userGradlePropertiesFile}' does not exist."
          )
      FileEditorManagerEx.getInstanceEx(project).openFile(userGradlePropertiesVf, true)
    }
  }

  // -- Private Methods ----------------------------------------------------- //
  // -- Inner Type ---------------------------------------------------------- //
}

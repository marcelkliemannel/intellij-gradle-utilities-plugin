package dev.turingcomplete.intellijgradleutilitiesplugin.common.ui

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project

internal object NotificationUtils {
  // -- Variables ----------------------------------------------------------- //
  // -- Initialization ------------------------------------------------------ //
  // -- Exposed Methods ----------------------------------------------------- //

  fun notifyError(title: String, message: String, project: Project? = null) {
    ApplicationManager.getApplication().invokeLater {
      NotificationGroupManager.getInstance()
        .getNotificationGroup(
          "dev.turingcomplete.intellij-gradle-utilities-plugin.notification-group"
        )
        .createNotification(title, message, NotificationType.ERROR)
        .notify(project)
    }
  }

  // -- Private Methods ----------------------------------------------------- //
  // -- Inner Type ---------------------------------------------------------- //
}

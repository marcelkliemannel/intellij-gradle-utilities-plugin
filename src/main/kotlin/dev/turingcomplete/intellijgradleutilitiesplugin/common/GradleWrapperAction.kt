package dev.turingcomplete.intellijgradleutilitiesplugin.common

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.NlsActions
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

abstract class GradleWrapperAction<R>(title: @NlsActions.ActionText String,
                                      description: @NlsActions.ActionDescription String? = null,
                                      icon: Icon? = null)
  : GradleUtilityAction<R>(title, description, icon, executionMode = ExecutionMode.MODAL) {

  // -- Companion Object -------------------------------------------------------------------------------------------- //
  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  protected fun findGradlewExecutable(projectDir: VirtualFile, progressIndicator: ProgressIndicator): VirtualFile? {
    progressIndicator.checkCanceled()
    return GradleUtils.findGradlewExecutable(projectDir)
  }

  protected fun determineGradleWrapperVersion(projectDir: VirtualFile,
                                              gradlewExecutable: VirtualFile,
                                              progressIndicator: ProgressIndicator): String? {
    progressIndicator.checkCanceled()
    return GradleUtils.determineGradleVersion(projectDir.toNioPath(), gradlewExecutable.toNioPath())
  }

  protected fun findGradleWrapperJar(projectDir: VirtualFile, progressIndicator: ProgressIndicator): VirtualFile? {
    progressIndicator.checkCanceled()
    return projectDir.findFileByRelativePath("gradle/wrapper/gradle-wrapper.jar")
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}
package dev.turingcomplete.intellijgradleutilitiesplugin.common

class GradleUtilityActionFailedException(override val message: String, cause: Throwable? = null) :
  RuntimeException(message, cause)

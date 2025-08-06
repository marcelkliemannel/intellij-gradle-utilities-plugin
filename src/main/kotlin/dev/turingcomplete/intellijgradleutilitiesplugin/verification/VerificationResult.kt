package dev.turingcomplete.intellijgradleutilitiesplugin.verification

import java.nio.file.Path

class VerificationResult(val fileVerificationResults: List<File>, val errors: List<String>) {
  // -- Companion Object ---------------------------------------------------- //
  // -- Properties ---------------------------------------------------------- //
  // -- Initialization ------------------------------------------------------ //
  // -- Exported Methods ---------------------------------------------------- //
  // -- Private Methods ----------------------------------------------------- //
  // -- Inner Type ---------------------------------------------------------- //

  class File(
    val file: Path,
    val title: String,
    val actualChecksum: String,
    val expectedChecksum: String,
    val checksumFileUrl: String,
    val matchingMessage: String = "The SHA-256 checksum of the file matches the expected one.",
    val errorMessage: String = "The SHA-256 checksum of the file does not match the expected one.",
    val warnings: List<Warning> = listOf(),
  )

  class Warning(val text: String, val learnMoreLink: String)
}

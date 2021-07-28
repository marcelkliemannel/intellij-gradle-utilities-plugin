import org.jetbrains.changelog.closure
import org.jetbrains.changelog.date

fun properties(key: String) = project.findProperty(key).toString()

plugins {
  java
  kotlin("jvm") version "1.5.10"
  id("org.jetbrains.intellij") version "1.1.4"
  id("org.jetbrains.changelog") version "1.1.2"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

repositories {
  mavenCentral()
}

intellij {
  version.set(properties("platformVersion"))
  type.set(properties("platformType"))
  downloadSources.set(properties("platformDownloadSources").toBoolean())
  updateSinceUntilBuild.set(true)
  plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

changelog {
  version = project.version as String
  header = closure { "[$version] - ${date()}" }
  groups = listOf("Added", "Changed", "Removed", "Fixed")
}

tasks {
  patchPluginXml {
    version.set(properties("pluginVersion"))
    sinceBuild.set(properties("pluginSinceBuild"))
    untilBuild.set(properties("pluginUntilBuild"))
    changeNotes.set(provider { changelog.getLatest().toHTML() })
  }

  runPluginVerifier {
    ideVersions.set(properties("pluginVerifierIdeVersions").split(',').map(String::trim).filter(String::isNotEmpty))
  }

  publishPlugin {
    dependsOn("patchChangelog")
    token.set("TOKEN")
    channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
  }

  signPlugin {
    certificateChain.set("""
-----BEGIN CERTIFICATE-----
-----END CERTIFICATE-----
  """.trimIndent())

    privateKey.set("""
-----BEGIN ENCRYPTED PRIVATE KEY-----
-----END ENCRYPTED PRIVATE KEY-----
  """.trimIndent())

    password.set("PASSWORD")
  }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      freeCompilerArgs = listOf("-Xjsr305=strict")
      jvmTarget = "11"
    }
  }
}

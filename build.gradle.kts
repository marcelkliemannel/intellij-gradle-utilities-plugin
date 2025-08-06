import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask.FailureLevel.COMPATIBILITY_PROBLEMS
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask.FailureLevel.INTERNAL_API_USAGES
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask.FailureLevel.INVALID_PLUGIN
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask.FailureLevel.NON_EXTENDABLE_API_USAGES
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask.FailureLevel.OVERRIDE_ONLY_API_USAGES
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
  java
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.intellij.platform)
  alias(libs.plugins.changelog)
  alias(libs.plugins.version.catalog.update)
  alias(libs.plugins.spotless)
}

group = properties("pluginGroup")
version = properties("pluginVersion")
val platform = properties("platform")

repositories {
  mavenLocal()
  mavenCentral()

  intellijPlatform { defaultRepositories() }
}

dependencies {
  intellijPlatform {
    create(platform, properties("platformVersion")) {
      useInstaller.set(false)
    }
    bundledPlugins(properties("platformGlobalBundledPlugins").split(','))

    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.JUnit5)
  }
}

spotless { kotlin { ktfmt().googleStyle() } }

java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }

configurations.all {
  exclude(group = "org.slf4j", module = "slf4j-api")
  exclude(group = "org.slf4j", module = "slf4j-simple")
  exclude(group = "org.slf4j", module = "slf4j-log4j12")
  exclude(group = "org.slf4j", module = "slf4j-jdk14")
}

tasks {
  withType<KotlinCompile> {
    compilerOptions {
      freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.ExperimentalStdlibApi")
      jvmTarget.set(JvmTarget.JVM_21)
    }
  }

  withType<Test> {
    useJUnitPlatform()
    systemProperty("java.awt.headless", "false")
  }

  named("check") { dependsOn("spotlessCheck") }
}

dependencies {
  intellijPlatform {
    pluginVerifier()
    zipSigner()
  }

  testImplementation(libs.assertj.core)
  testImplementation(libs.bundles.junit.implementation)
  testRuntimeOnly(libs.bundles.junit.runtime)
}

intellijPlatform {
  pluginConfiguration {
    id = providers.gradleProperty("pluginId")
    version = providers.gradleProperty("pluginVersion")

    ideaVersion {
      sinceBuild = properties("pluginSinceBuild")
      untilBuild = provider { null }
    }

    changeNotes.set(
        provider {
          changelog.renderItem(
              changelog.get(project.version as String),
              Changelog.OutputType.HTML,
          )
        })
  }

  signing {
    val jetbrainsDir = File(System.getProperty("user.home"), ".jetbrains")
    certificateChain.set(
        project.provider { File(jetbrainsDir, "plugin-sign-chain.crt").readText() })
    privateKey.set(
        project.provider { File(jetbrainsDir, "plugin-sign-private-key.pem").readText() })
    password.set(project.provider { properties("jetbrains.sign-plugin.password") })
  }

  publishing {
    token.set(project.provider { properties("jetbrains.marketplace.token") })
    channels.set(
        listOf(
            properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
  }

  pluginVerification {
    failureLevel.set(
        listOf(
            COMPATIBILITY_PROBLEMS,
            INTERNAL_API_USAGES,
            NON_EXTENDABLE_API_USAGES,
            OVERRIDE_ONLY_API_USAGES,
            INVALID_PLUGIN,
            // Will fail for non-IC IDEs
            // MISSING_DEPENDENCIES
        ))

    ides {
      recommended()
    }
  }
}

changelog {
  val projectVersion = project.version as String
  version.set(projectVersion)
  header.set("$projectVersion - ${org.jetbrains.changelog.date()}")
  groups.set(listOf("Added", "Changed", "Removed", "Fixed"))
}

tasks {
  named("publishPlugin") {
    dependsOn("check")

    doFirst { check(platform == "IC") { "Expected platform 'IC', but was: '$platform'" } }
  }

  named("buildSearchableOptions") { enabled = false }
}

versionCatalogUpdate {
  pin {
    versions.set(
        listOf(
            // Must be updated in conjunction with the minimum platform version
            // https://plugins.jetbrains.com/docs/intellij/using-kotlin.html#kotlin-standard-library
            "kotlin"))
  }
}

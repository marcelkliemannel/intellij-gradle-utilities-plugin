import org.jetbrains.changelog.closure
import org.jetbrains.changelog.date

plugins {
  java
  kotlin("jvm") version "1.5.0"
  id("org.jetbrains.intellij") version "0.7.2"
  id("org.jetbrains.changelog") version "1.1.2"
}

group = "dev.turingcomplete"
version = "1.1.0"

repositories {
  mavenCentral()
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
}

intellij {
  version = "2021.1"
  setPlugins("com.intellij.gradle")
}

changelog {
  version = project.version as String
  header = closure { "[$version] - ${date()}" }
  groups = listOf("Added", "Changed", "Removed", "Fixed")
}

tasks {
  patchPluginXml {
    changeNotes(closure { changelog.get(project.version as String).toHTML() })
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "11"
  }
}

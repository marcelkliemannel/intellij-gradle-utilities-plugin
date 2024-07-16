package dev.turingcomplete.intellijgradleutilitiesplugin.managegradleuserhome.gradledependencies

import dev.turingcomplete.intellijgradleutilitiesplugin.common.Directory
import org.apache.commons.io.FileUtils

data class GradleDep(val group: String, val name: String, val version: String, val dirs: List<Directory>) {

    val size: Long?
        get() {
            var size: Long? = null
            for (dir in dirs) {
                if (dir.size != null) {
                    size = (size ?: 0L) + dir.size
                }
            }
            return size
        }

    val searchName = "$group:$name:$version"

}

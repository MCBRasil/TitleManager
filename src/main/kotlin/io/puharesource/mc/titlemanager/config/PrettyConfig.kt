package io.puharesource.mc.titlemanager.config

import com.google.common.io.Files
import io.puharesource.mc.titlemanager.pluginInstance
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class PrettyConfig(file: File) : YamlConfiguration() {
    companion object {
        private val KEY_PATTERN = """^([ ]*)([^'"]+)[:].*$""".toRegex()
        private val COMMENT_PATTERN = """^([ ]*[#].*)|[ ]*$""".toRegex()
    }

    init {
        val resource = pluginInstance.getResource(file.name)

        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()

            if (resource != null) {
                file.writeBytes(resource.readBytes())
            }
        }

        load(file)
    }

    fun saveToStringWithComments(currentCommentedData: String) : String {
        val commentlessData = saveToString()
        val commentMap : MutableMap<String, List<String>> = mutableMapOf()

        val currentComments : MutableList<String> = mutableListOf()

        currentCommentedData.lines().forEachIndexed { i, line ->
            if (line.matches(KEY_PATTERN) && !line.matches(COMMENT_PATTERN)) {
                val result = KEY_PATTERN.matchEntire(line)!!.groupValues

                commentMap.put(result[1] + result[2], currentComments.toList())

                currentComments.clear()
            } else if (line.matches(COMMENT_PATTERN)) {
                currentComments.add(line)
            }
        }

        val sb = StringBuilder()

        commentlessData.lines().filter { !it.matches(COMMENT_PATTERN) }.forEach { line ->
            if (line.matches(KEY_PATTERN)) {
                val result = KEY_PATTERN.matchEntire(line)!!.groupValues

                commentMap[result[1] + result[2]]?.let {
                    it.forEach { sb.append(it).append('\n') }

                    sb.removeSuffix("\n")
                }
            }

            sb.append(line).append('\n')
        }

        return sb.toString().removeSuffix("\n")
    }

    override fun save(file: File) {
        Files.createParentDirs(file)

        val resource = pluginInstance.getResource(file.name)

        if (resource == null) {
            super.save(file)
        } else {
            val data = saveToStringWithComments(resource.reader().readText())

            file.writeText(data)
        }
    }

    override fun save(file: String) {
        save(File(file))
    }
}
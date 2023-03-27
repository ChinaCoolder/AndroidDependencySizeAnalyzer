package happy.jyc.android_dependency_analyzer

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Internal
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.Scanner
import java.util.zip.ZipFile

abstract class BaseTask : DefaultTask() {
    companion object {
        const val ANDROID_DEPENDENCY_COMMAND = "androidDependencies"
        const val DEPENDENCY_FILE_NAME = "jycAndroidDependency.txt"
        const val PAD_END = 120
    }

    @Internal
    override fun getGroup(): String = "JYCAndroidAnalyzer"

    protected data class ClassPath(
        val size: Long,
        val name: String,
        val libList: List<Libraries>
    ) {
        companion object {
            fun convert(
                file: File,
                dependencyFilter: List<String>,
                cache: String?
            ): List<ClassPath> {
                val result = mutableListOf<ClassPath>()
                Scanner(file).use { scanner ->
                    var classPath = ""
                    var size = 0L
                    var libLit = mutableListOf<Libraries>()
                    while (scanner.hasNextLine()) {
                        val line = scanner.nextLine()
                        if (classPath.isEmpty()) {
                            if (line.contains("- Dependencies for")) {
                                classPath = line.split("-")[0].trim()
                            }
                        } else {
                            if (
                                (line.startsWith("+---") || line.startsWith("\\---")) &&
                                (line.endsWith("@jar") || line.endsWith("@aar"))
                            ) {
                                var library: Libraries?
                                try {
                                    library = Libraries.convert(line, dependencyFilter, cache)
                                    size += library.size
                                    libLit.add(library)
                                } catch (_: IllegalStateException){}
                                catch (e: Exception) {
                                    println(e.message.orEmpty())
                                }
                                if (line.startsWith("\\---")) {
                                    result.add(ClassPath(size, classPath, libLit))
                                    size = 0L
                                    libLit = mutableListOf()
                                    classPath = ""
                                }
                            }
                        }
                    }
                }
                return result
            }
        }
    }

    protected data class Libraries(
        val size: Long,
        val name: String,
        val isAar: Boolean
    ) {
        companion object {
            fun translateToFile(
                packageName: String,
                id: String,
                version: String,
                cache: String?
            ) =
                File(
                    "${cache ?: "${System.getProperty("user.home")}${File.separator}.gradle${File.separator}caches"}${File.separator}modules-2${File.separator}files-2.1${File.separator}" +
                            "$packageName${File.separator}$id${File.separator}$version${File.separator}"
                )

            fun convert(
                line: String,
                dependencyFilter: List<String>,
                cache: String?
            ): Libraries {
                val lineSplit = line.split("@")[0].split(":")
                val packageName = lineSplit[0].split(" ")[1]
                val id = lineSplit[1]
                val version = lineSplit[2]
                val name = "${packageName}:${id}:$version"

                if (dependencyFilter.isNotEmpty() && dependencyFilter.find { packageName.contains(it) } == null) {
                    throw IllegalStateException("this dependency not match to filter")
                }

                val file = translateToFile(packageName, id, version, cache)
                file.listFiles()?.forEach { childDir ->
                    childDir.listFiles()?.filter { it.isFile && (it.name.endsWith("${version}.jar") || it.name.endsWith("${version}.aar")) }?.forEach { child ->
                        return Libraries(child.length(), name, line.endsWith("aar"))
                    }
                }
                return Libraries(-1L, name, line.endsWith("aar"))
            }
        }
    }

    protected data class AAR(
        val size: Long,
        val name: String,
        val aarFiles: MutableList<AARFiles>
    ) {
        companion object {
            fun convert(
                fullName: String,
                ext: List<String>,
                filter: List<String>,
                cache: String?
            ): AAR {
                val file = Libraries.translateToFile(
                    fullName.split(":")[0],
                    fullName.split(":")[1],
                    fullName.split(":")[2],
                    cache)
                if (file.exists()) {
                    var target: File? = null
                    file.listFiles()?.forEach { child ->
                        if (target == null) {
                            child.listFiles()?.forEach {
                                if (target == null) {
                                    if (it.isFile && it.name.lowercase().endsWith("${fullName.split(":")[2]}.aar")) {
                                        target = it
                                    }
                                }
                            }
                        }
                    }
                    if (target != null) {
                        val list = mutableListOf<AARFiles>()
                        ZipFile(target!!).use { zip ->
                            zip.entries().let {entries ->
                                while (entries.hasMoreElements()) {
                                    val entry = entries.nextElement().takeIf { entry ->
                                        !entry.isDirectory &&
                                            (ext.isEmpty() || ext.find { entry.name.lowercase().endsWith(it) } != null) &&
                                                (filter.isEmpty() || filter.find { entry.name.contains(it) } != null)
                                    }
                                    if (entry!= null) {
                                        list.add(
                                            AARFiles(
                                                entry.size,
                                                entry.name
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        list.sortBy {
                            - it.size
                        }
                        return AAR(
                            list.sumOf { it.size },
                            fullName,
                            list
                        )
                    } else {
                        throw IllegalArgumentException("can't find target aar file")
                    }
                } else {
                    throw IllegalArgumentException("can't find aar directory in target cache path")
                }
            }
        }
    }

    protected data class AARFiles(
        val size: Long,
        val name: String
    )
}
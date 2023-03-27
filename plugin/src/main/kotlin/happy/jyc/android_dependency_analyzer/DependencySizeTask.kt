package happy.jyc.android_dependency_analyzer

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

abstract class DependencySizeTask : BaseTask() {

    @get:Option(description = "target module that need to be analyze", option = "name")
    @get:Input
    abstract val moduleName: Property<String>

    @set:Option(description = "target class path that need to be analyze", option = "classpath")
    @get:Input
    @get:Optional
    abstract var classPathName: List<String>?

    @set:Option(description = "dependency filter", option = "filter")
    @get:Input
    @get:Optional
    abstract var filters: List<String>?

    @get:Option(description = "the path where gradlew file exist, default is in project's root path", option = "gradlew")
    @get:Input
    @get:Optional
    abstract val gradlew: Property<String>

    @get:Option(description = "the path where gradle cache all files, default is {user_home}/.gradle/caches/", option = "cache")
    @get:Input
    @get:Optional
    abstract val gradleCache: Property<String>

    @Internal
    override fun getDescription(): String =
        "list all the dependency and it's size of class path"

    @TaskAction
    fun list() {
        if (project.tasks.findByName(ANDROID_DEPENDENCY_COMMAND) != null) {
            var dependencyFile = File(project.buildDir.parentFile.parentFile.absolutePath + File.separator + DEPENDENCY_FILE_NAME)
            if (dependencyFile.exists()) {
                dependencyFile.delete()
            }
            Runtime.getRuntime().exec("${
                gradlew.takeIf { it.isPresent }?.get() ?: 
                (project.buildDir.parentFile.parentFile.absolutePath + File.separator)}${if (System.getProperty("os.name").contains("Windows"))"gradlew.bat" else "gradlew"} ${moduleName.get()}:$ANDROID_DEPENDENCY_COMMAND >$DEPENDENCY_FILE_NAME").waitFor()
            dependencyFile = File(project.buildDir.parentFile.parentFile.absolutePath + File.separator + DEPENDENCY_FILE_NAME)
            if (dependencyFile.exists()) {
                ClassPath.convert(dependencyFile, filters?: listOf(), gradleCache.takeIf { it.isPresent }?.get()).filter {
                    classPathName.isNullOrEmpty() || it.name in classPathName!!
                }.sortedBy {
                    - it.size
                }.forEach { classPath ->
                    println("For classpath ${classPath.name}:")
                    println(
                        "Total dependencies size :".padEnd(PAD_END) + "${
                            String.format(
                                "%.2f",
                                classPath.size.toDouble() / (1024 * 1024)
                            )
                        } mb"
                    )
                    classPath.libList.sortedBy {
                        - it.size
                    }.forEach {
                        println("${(it.name + if (it.isAar) "@aar" else "@jar").padEnd(PAD_END)}${if (it.size == -1L) "UNKNOWN" else String.format("%.2f", it.size.toDouble() / 1024.0)} kb")
                    }
                    println()
                }
                dependencyFile.delete()
            } else {
                println("Create dependency file failed")
            }
        } else {
            println("Task $ANDROID_DEPENDENCY_COMMAND not found")
        }
    }
}
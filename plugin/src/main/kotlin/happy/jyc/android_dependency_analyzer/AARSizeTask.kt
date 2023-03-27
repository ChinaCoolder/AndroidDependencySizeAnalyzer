package happy.jyc.android_dependency_analyzer

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class AARSizeTask: BaseTask() {
    @set:Option(description = "target aar that need to be analyze", option = "aar")
    @get:Input
    abstract var aarName: List<String>?

    @set:Option(description = "aar content file extension filter", option = "ext")
    @get:Input
    @get:Optional
    abstract var ext: List<String>?

    @set:Option(description = "aar content file name filter", option = "filter")
    @get:Input
    @get:Optional
    abstract var filters: List<String>?

    @get:Option(description = "the path where gradle cache all files, default is {user_home}/.gradle/caches/", option = "cache")
    @get:Input
    @get:Optional
    abstract val gradleCache: Property<String>

    @Internal
    override fun getDescription(): String =
        "analyze aar file's size"

    @TaskAction
    fun listAllAndroidDependency() {
        try {
            val list = mutableListOf<AAR>()
            aarName?.forEach { aar ->
                list.add(AAR.convert(aar, ext?: listOf(), filters?: listOf(), gradleCache.takeIf { it.isPresent }?.get()))
            }
            list.sortBy { -it.size }
            list.forEach { aar ->
                val sb = StringBuffer()
                sb.append("For aar ${aar.name}:\n")
                sb.append("Total size:".padEnd(PAD_END))
                sb.append(String.format(
                    "%.2f",
                    aar.size.toDouble() / (1024 * 1024)
                ))
                sb.append(" mb\n")
                aar.aarFiles.forEach{ file ->
                    sb.append(file.name.padEnd(PAD_END))
                    sb.append(String.format("%.2f", file.size.toDouble() / 1024.0))
                    sb.append(" kb\n")
                }
                println(sb.toString())
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }
}
/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package happy.jyc.android_dependency_analyzer

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * A simple unit test for the 'happy.jyc.android_dependency_analyzer.greeting' plugin.
 */
class AndroidDependencyAnalyzerPluginTest {
    @Test fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("happy.jyc.android_dependency_analyzer")

        // Verify the result
        assertNotNull(project.tasks.findByName("jycDependencySize"))
        assertNotNull(project.tasks.findByName("jycAARAnalyze"))
    }
}

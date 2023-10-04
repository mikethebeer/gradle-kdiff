package at.mibe.gradle.kdiff

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

class GradleKDiffPluginTest {
    @Test fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("at.mibe.gradle.kdiff")

        // Verify the result
        assertNotNull(project.tasks.findByName("kDiffVersion"))
    }
}

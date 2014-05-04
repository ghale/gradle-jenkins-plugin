package com.terrafolio.gradle.plugins.jenkins.test.service

import com.terrafolio.gradle.plugins.jenkins.service.BuildDirService
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

/**
 * Created by ghale on 5/3/14.
 */
class BuildDirServiceTest {
    def private final Project project = ProjectBuilder.builder().withProjectDir(new File('build/tmp/test')).build()

    @Test
    def void "makeAndGetDir returns new directory"() {
        def BuildDirService service = BuildDirService.forProject(project)
        def File dir = service.makeAndGetDir("newdir")
        assert dir.exists()
    }

    @Test
    def void "makeAndGetDir returns existing directory"() {
        def BuildDirService service = BuildDirService.forProject(project)
        File newDir = new File(project.buildDir, "existing")
        assert newDir.mkdirs()
        def File dir = service.makeAndGetDir("existing")
        assert dir.equals(newDir)
    }
}

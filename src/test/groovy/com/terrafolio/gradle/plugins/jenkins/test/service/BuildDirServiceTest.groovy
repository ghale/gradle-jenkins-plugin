package com.terrafolio.gradle.plugins.jenkins.test.service

import com.terrafolio.gradle.plugins.jenkins.service.BuildDirService
import nebula.test.ProjectSpec

/**
 * Created by ghale on 5/3/14.
 */
class BuildDirServiceTest extends ProjectSpec{
    def BuildDirService service

    def setup() {
        service = BuildDirService.forProject(project)
    }

    def void "makeAndGetDir returns new directory"() {
        expect:
        service.makeAndGetDir("newdir").exists()
    }

    def void "makeAndGetDir returns existing directory"() {
        setup:
        File newDir = new File(project.buildDir, "existing")
        newDir.mkdirs()

        expect:
        newDir.exists()
        service.makeAndGetDir("existing").equals(newDir)
    }
}

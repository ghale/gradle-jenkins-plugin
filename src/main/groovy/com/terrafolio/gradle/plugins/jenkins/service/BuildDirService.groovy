package com.terrafolio.gradle.plugins.jenkins.service

import org.gradle.api.Project

class BuildDirService {

    private Project project

    static def forProject(Project project) {
        return new BuildDirService(project: project)
    }

    File makeAndGetDir(String dir) {
        def jobDir = new File(project.buildDir, dir)
        if (! jobDir.exists()) {
            jobDir.mkdirs()
        }
        return jobDir
    }
}

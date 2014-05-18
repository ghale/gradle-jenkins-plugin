package com.terrafolio.gradle.plugins.jenkins.tasks

import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurable
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsJob
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsView
import com.terrafolio.gradle.plugins.jenkins.service.BuildDirService

/**
 * Created by ghale on 5/18/14.
 */
abstract class AbstractDumpJenkinsItemsTask extends AbstractJenkinsTask {
    @Override
    public void doExecute() {
        def buildDirService = BuildDirService.forProject(project)
        getAllItems().findAll { it instanceof JenkinsJob }.each { job ->
            writeXmlConfigurations(job, buildDirService, "jobs")
        }

        getAllItems().findAll { it instanceof JenkinsView }.each { view ->
            writeXmlConfigurations(view, buildDirService, "views")
        }
    }

    def void dump(JenkinsConfigurable item) {
        items += item
    }

    def void dump(Closure closure) {
        itemClosures += closure
    }

    abstract public void writeXmlConfigurations(JenkinsConfigurable item, BuildDirService buildDirService, String itemType)
}

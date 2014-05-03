package com.terrafolio.gradle.plugins.jenkins.tasks

import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurable
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsServerDefinition
import com.terrafolio.gradle.plugins.jenkins.service.BuildDirService
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsService

class DumpRemoteJenkinsItemsTask extends AbstractJenkinsTask {

    @Override
    void doExecute() {
        def buildDirService = BuildDirService.forProject(project)

        getJobs().each { job ->
            writeXmlConfigurations(job, buildDirService, "jobs")
        }

        getViews().each { view ->
            writeXmlConfigurations(view, buildDirService, "views")
        }
    }

    public void writeXmlConfigurations(JenkinsConfigurable item, BuildDirService buildDir, String itemTypeDir) {
        eachServer(item) { JenkinsServerDefinition server, JenkinsService service ->
            String serverStrItem = service.getConfiguration(item.configurableName, item.serviceOverrides.get)

            if (serverStrItem == null) {
                return
            }

            // item.name is not a super class field. consider to refactor
            def file = new File(buildDir.makeAndGetDir("remotes/${server.name}/$itemTypeDir"), "${item.name}.xml")

            file.write(serverStrItem)
        }
    }
}

package com.terrafolio.gradle.plugins.jenkins.tasks

import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurable
import com.terrafolio.gradle.plugins.jenkins.service.BuildDirService

class DumpJenkinsJobsTask extends AbstractJenkinsTask {
    def prettyPrint = true

    public DumpJenkinsJobsTask() {
        super();
        needsCredentials = false
    }

    @Override
    public void doExecute() {
        def buildDirService = BuildDirService.forProject(project)
        getJobs().each { job ->
            writeXmlConfigurations(job, buildDirService, "jobs")
        }

        getViews().each { view ->
            writeXmlConfigurations(view, buildDirService, "views")
        }
    }

    public void writeXmlConfigurations(JenkinsConfigurable item, BuildDirService buildDirService, String itemType) {
        getServerDefinitions(item).each { server ->
            def file = new File(buildDirService.makeAndGetDir("${server.name}/${itemType}"), "${item.name}.xml")
            if (prettyPrint) {
                file.withWriter { fileWriter ->
                    def node = new XmlParser().parseText(item.getServerSpecificXml(server));
                    new XmlNodePrinter(new PrintWriter(fileWriter)).print(node)
                }
            } else {
                def xml = item.getServerSpecificXml(server)
                file.write(xml)
            }
        }
    }
}
